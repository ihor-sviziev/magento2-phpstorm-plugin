/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.CreateAPluginAction;
import com.magento.idea.magento2plugin.actions.generation.data.PluginDiXmlData;
import com.magento.idea.magento2plugin.actions.generation.data.PluginFileData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.CreateAPluginDialogValidator;
import com.magento.idea.magento2plugin.actions.generation.generator.PluginClassGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.PluginDiXmlGenerator;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.Plugin;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.ui.FilteredComboBox;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"PMD.TooManyFields", "PMD.DataClass", "PMD.UnusedPrivateMethod"})
public class CreateAPluginDialog extends AbstractDialog {
    @NotNull
    private final Project project;
    private final Method targetMethod;
    private final PhpClass targetClass;
    @NotNull
    private final CreateAPluginDialogValidator validator;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField pluginClassName;
    private JTextField pluginDirectory;
    private JComboBox pluginType;
    private FilteredComboBox pluginModule;
    private JComboBox pluginArea;
    private JTextField pluginSortOrder;
    private JTextField pluginName;
    private JLabel pluginDirectoryName;//NOPMD
    private JLabel selectPluginModule;//NOPMD
    private JLabel pluginTypeLabel;//NOPMD
    private JLabel pluginAreaLabel;//NOPMD
    private JLabel pluginNameLabel;//NOPMD
    private JLabel pluginClassNameLabel;//NOPMD
    private JLabel pluginSortOrderLabel;//NOPMD

    /**
     * Constructor.
     *
     * @param project Project
     * @param targetMethod Method
     * @param targetClass PhpClass
     */
    public CreateAPluginDialog(
            final @NotNull Project project,
            final Method targetMethod,
            final PhpClass targetClass
    ) {
        super();
        this.project = project;
        this.targetMethod = targetMethod;
        this.targetClass = targetClass;
        this.validator = CreateAPluginDialogValidator.getInstance(this);

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        fillPluginTypeOptions();
        fillTargetAreaOptions();

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent event) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent event) {
                onCancel();
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent event) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(final ActionEvent event) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );
    }

    private void fillPluginTypeOptions() {
        for (final Plugin.PluginType pluginPrefixType: Plugin.PluginType.values()) {
            pluginType.addItem(pluginPrefixType.toString());
        }
    }

    private void fillTargetAreaOptions() {
        for (final Areas area : Areas.values()) {
            pluginArea.addItem(area.toString());
        }
    }

    protected void onOK() {
        if (!validator.validate(project)) {
            return;
        }
        new PluginClassGenerator(new PluginFileData(
                getPluginDirectory(),
                getPluginClassName(),
                getPluginType(),
                getPluginModule(),
                targetClass,
                targetMethod,
                getPluginClassFqn(),
                getNamespace()
        ), project).generate(CreateAPluginAction.ACTION_NAME, true);

        new PluginDiXmlGenerator(new PluginDiXmlData(
                getPluginArea(),
                getPluginModule(),
                targetClass,
                getPluginSortOrder(),
                getPluginName(),
                getPluginClassFqn()
        ), project).generate(CreateAPluginAction.ACTION_NAME);

        this.setVisible(false);
    }

    public String getPluginName() {
        return this.pluginName.getText().trim();
    }

    public String getPluginSortOrder() {
        return this.pluginSortOrder.getText().trim();
    }

    public String getPluginClassName() {
        return this.pluginClassName.getText().trim();
    }

    public String getPluginDirectory() {
        return this.pluginDirectory.getText().trim();
    }

    public String getPluginArea() {
        return this.pluginArea.getSelectedItem().toString();
    }

    public String getPluginType() {
        return this.pluginType.getSelectedItem().toString();
    }

    public String getPluginModule() {
        return this.pluginModule.getSelectedItem().toString();
    }

    /**
     * Open an action dialog.
     *
     * @param project Project
     * @param targetMethod Method
     * @param targetClass PhpClass
     */
    public static void open(
            final @NotNull Project project,
            final Method targetMethod,
            final PhpClass targetClass
    ) {
        final CreateAPluginDialog dialog = new CreateAPluginDialog(
                project,
                targetMethod,
                targetClass
        );
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    private void createUIComponents() {
        final List<String> allModulesList = ModuleIndex.getInstance(project)
                .getEditableModuleNames();

        this.pluginModule = new FilteredComboBox(allModulesList);
    }

    private String getNamespace() {
        final String targetModule = getPluginModule();
        String namespace = targetModule.replace(
                Package.vendorModuleNameSeparator,
                Package.fqnSeparator
        );
        namespace = namespace.concat(Package.fqnSeparator);
        return namespace.concat(getPluginDirectory().replace(File.separator, Package.fqnSeparator));
    }

    private String getPluginClassFqn() {
        return getNamespace().concat(Package.fqnSeparator).concat(getPluginClassName());
    }
}
