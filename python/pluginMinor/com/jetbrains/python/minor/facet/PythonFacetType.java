// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.python.minor.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.FacetType;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.python.facet.PythonFacetSettings;
import com.jetbrains.python.sdk.PythonSdkType;
import icons.PythonIcons;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

import static com.jetbrains.python.PyNames.PYTHON_MODULE_ID;

/**
 * @author traff
 */
public class PythonFacetType extends FacetType<PythonFacet, PythonFacetType.PythonFacetConfiguration> {

  @NonNls
  private static final String ID = "Python";

  public static PythonFacetType getInstance() {
    return findInstance(PythonFacetType.class);
  }

  public PythonFacetType() {
    super(PythonFacet.ID, ID, "Python");
  }

  @Override
  public PythonFacetConfiguration createDefaultConfiguration() {
    PythonFacetConfiguration result = new PythonFacetConfiguration();
    List<Sdk> sdks = ProjectJdkTable.getInstance().getSdksOfType(PythonSdkType.getInstance());
    if (sdks.size() > 0) {
      result.setSdk(sdks.get(0));
    }
    return result;
  }

  @Override
  public PythonFacet createFacet(@NotNull Module module,
                                 String name,
                                 @NotNull PythonFacetConfiguration configuration,
                                 @Nullable Facet underlyingFacet) {
    return new PythonFacet(this, module, name, configuration, underlyingFacet);
  }

  @Override
  public boolean isSuitableModuleType(ModuleType moduleType) {
    return !(moduleType.getId().equals(PYTHON_MODULE_ID));
  }

  @Override
  public Icon getIcon() {
    return PythonIcons.Python.Python;
  }

  public static class PythonFacetConfiguration extends PythonFacetSettings implements FacetConfiguration {
    private static final String SDK_NAME = "sdkName";

    @Override
    public FacetEditorTab[] createEditorTabs(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
      return new FacetEditorTab[]{};
    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {
      String sdkName = element.getAttributeValue(SDK_NAME);
      mySdk = StringUtil.isEmpty(sdkName) ? null : ProjectJdkTable.getInstance().findJdk(sdkName, PythonSdkType.getInstance().getName());

      if (mySdk != null) {
        ApplicationManager.getApplication().getMessageBus().syncPublisher(ProjectJdkTable.JDK_TABLE_TOPIC).jdkAdded(mySdk);
      }
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
      element.setAttribute(SDK_NAME, mySdk == null ? "" : mySdk.getName());
    }
  }
}
