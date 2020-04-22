/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.NewGraphQlResolverAction;
import com.magento.idea.magento2plugin.actions.generation.data.GraphQlResolverFileData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.NewGraphQlResolverValidator;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleGraphQlResolverClassGenerator;
import com.magento.idea.magento2plugin.magento.files.GraphQlResolverPhp;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectory;
import javax.swing.*;
import java.awt.event.*;
import com.magento.idea.magento2plugin.magento.packages.File;

public class NewGraphQlResolverDialog extends AbstractDialog {
    private final NewGraphQlResolverValidator validator;
    private final PsiDirectory baseDir;
    private final GetModuleNameByDirectory getModuleNameByDir;
    private final String moduleName;
    private JPanel contentPanel;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField graphQlResolverClassName;
    private JTextField graphQlResolverParentDir;
    private Project project;

    public NewGraphQlResolverDialog(Project project, PsiDirectory directory) {
        this.project = project;
        this.baseDir = directory;
        this.moduleName = GetModuleNameByDirectory.getInstance(project).execute(directory);
        this.validator = NewGraphQlResolverValidator.getInstance(this);
        this.getModuleNameByDir = GetModuleNameByDirectory.getInstance(project);

        setContentPane(contentPanel);
        setModal(true);
        setTitle("Create a new Magento 2 GraphQL Resolver.");
        getRootPane().setDefaultButton(buttonOK);
        pushToMiddle();
        suggestGraphQlResolverDirectory();

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPanel.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    public static void open(Project project, PsiDirectory directory) {
        NewGraphQlResolverDialog dialog = new NewGraphQlResolverDialog(project, directory);
        dialog.pack();
        dialog.setVisible(true);
    }

    private void onOK() {
        if (!validator.validate()) {
            return;
        }
        generateFile();
        this.setVisible(false);
    }

    private PsiFile generateFile() {
        return new ModuleGraphQlResolverClassGenerator(new GraphQlResolverFileData(
                getGraphQlResolverDirectory(),
                getGraphQlResolverClassName(),
                getModuleName(),
                getGraphQlResolverClassFqn(),
                getNamespace()
        ), project).generate(NewGraphQlResolverAction.ACTION_NAME, true);
    }

    private String getModuleName() {
        return moduleName;
    }

    public String getGraphQlResolverClassName() {
        return graphQlResolverClassName.getText().trim();
    }

    public String getGraphQlResolverDirectory() {
        return graphQlResolverParentDir.getText().trim();
    }

    private void suggestGraphQlResolverDirectory() {
        String path = baseDir.getVirtualFile().getPath();
        String moduleIdentifierPath = getModuleIdentifierPath();
        if (moduleIdentifierPath == null) {
            graphQlResolverParentDir.setText(GraphQlResolverPhp.DEFAULT_DIR);
            return;
        }
        String[] pathParts = path.split(moduleIdentifierPath);
        if (pathParts.length != 2) {
            graphQlResolverParentDir.setText(GraphQlResolverPhp.DEFAULT_DIR);
            return;
        }

        if (pathParts[1] != null) {
            graphQlResolverParentDir.setText(pathParts[1].substring(1));
            return;
        }
        graphQlResolverParentDir.setText(GraphQlResolverPhp.DEFAULT_DIR);
    }

    private String getModuleIdentifierPath() {
        String[]parts = moduleName.split(Package.VENDOR_MODULE_NAME_SEPARATOR);
        if (parts[0] == null || parts[1] == null || parts.length > 2) {
            return null;
        }
        return parts[0] + File.separator + parts[1];
    }

    private String getNamespace() {
        String[]parts = moduleName.split(Package.VENDOR_MODULE_NAME_SEPARATOR);
        if (parts[0] == null || parts[1] == null || parts.length > 2) {
            return null;
        }
        String directoryPart = getGraphQlResolverDirectory().replace(File.separator, Package.FQN_SEPARATOR);
        return parts[0] + Package.FQN_SEPARATOR + parts[1] + Package.FQN_SEPARATOR + directoryPart;
    }

    private String getGraphQlResolverClassFqn() {
        return getNamespace().concat(Package.FQN_SEPARATOR).concat(getGraphQlResolverClassName());
    }

    public void onCancel() {
        // add your code here if necessary
        dispose();
    }
}
