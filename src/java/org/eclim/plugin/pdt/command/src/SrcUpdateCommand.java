/**
 * Copyright (c) 2005 - 2008
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclim.plugin.pdt.command.src;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.command.filter.ErrorFilter;

import org.eclim.eclipse.EclimPlugin;

import org.eclim.plugin.wst.command.validate.Reporter;
import org.eclim.plugin.wst.command.validate.ValidationContext;

import org.eclim.util.ProjectUtils;

import org.eclim.util.file.FileOffsets;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import org.eclipse.jface.text.reconciler.DirtyRegion;

import org.eclipse.php.internal.core.phpModel.parser.PHPWorkspaceModelManager;

import org.eclipse.php.internal.ui.editor.PHPStructuredTextViewer;

import org.eclipse.php.internal.ui.editor.validation.PHPValidator;

import org.eclipse.wst.sse.core.StructuredModelManager;

import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;

import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;

import org.eclipse.wst.validation.internal.provisional.core.IMessage;

/**
 * Command to update and optionally validate a php src file.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class SrcUpdateCommand
  extends AbstractCommand
{
  private static final PHPValidator VALIDATOR = new PHPValidator();

  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    String file = _commandLine.getValue(Options.FILE_OPTION);
    String projectName = _commandLine.getValue(Options.PROJECT_OPTION);

    IProject project = ProjectUtils.getProject(projectName, true);
    IFile ifile = ProjectUtils.getFile(project, file);

    // ensure model is refreshed with latest version of the file.
    PHPWorkspaceModelManager.getInstance().addFileToModel(ifile);

    // validate the src file.
    if(_commandLine.hasOption(Options.VALIDATE_OPTION)){
      // force loading of PHPProjectModel
      PHPWorkspaceModelManager.getInstance().getModelForProject(project, true);

      String filepath = ProjectUtils.getFilePath(project, file);
      Reporter reporter = new Reporter();
      IStructuredModel model =
        StructuredModelManager.getModelManager().getModelForRead(ifile);
      IStructuredDocument document = model.getStructuredDocument();

      PHPStructuredTextViewer viewer = new PHPStructuredTextViewer(
          EclimPlugin.getShell(), null, null, false, 0);
      viewer.setDocument(document);
      viewer.setSelectedRange(0, 20);
      DirtyRegion region = new DirtyRegion(
          0, document.getLength(), DirtyRegion.INSERT, "");
      ValidationContext context = new ValidationContext(filepath);

      VALIDATOR.connect(document);
      VALIDATOR.validate(region, context, reporter);
      VALIDATOR.disconnect(document);

      FileOffsets offsets = FileOffsets.compile(filepath);
      ArrayList<Error> errors = new ArrayList<Error>();
      for(IMessage message : reporter.getMessages()){
        int[] lineColumn = offsets.offsetToLineColumn(message.getOffset());
        errors.add(new Error(
            message.getText(),
            filepath,
            lineColumn[0],
            lineColumn[1],
            message.getSeverity() != IMessage.HIGH_SEVERITY
        ));
      }
      return ErrorFilter.instance.filter(_commandLine, errors);
    }
    return StringUtils.EMPTY;
  }
}
