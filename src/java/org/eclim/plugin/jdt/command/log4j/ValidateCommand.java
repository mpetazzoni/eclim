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
package org.eclim.plugin.jdt.command.log4j;

import java.util.ArrayList;
import java.util.List;

import org.eclim.Services;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.command.filter.ErrorFilter;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclim.util.ProjectUtils;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

/**
 * Command to validate a log4j xml file.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class ValidateCommand
  extends org.eclim.command.xml.validate.ValidateCommand
{
  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    String project = _commandLine.getValue(Options.PROJECT_OPTION);
    String file = _commandLine.getValue(Options.FILE_OPTION);

    Log4jHandler handler = new Log4jHandler(
        JavaUtils.getJavaProject(project), file);

    List<Error> errors = super.validate(project, file, false, handler);
    errors.addAll(handler.getErrors());

    return ErrorFilter.instance.filter(_commandLine, errors);
  }

  private static class Log4jHandler
    extends DefaultHandler
  {
    //private static final String APPENDER = "appender";
    //private static final String APPENDER_REF = "appender-ref";
    private static final String CATEGORY = "category";
    private static final String CLASS = "class";
    private static final String LEVEL = "level";
    private static final String LOGGER = "logger";
    private static final String NAME = "name";
    private static final String PRIORITY = "priority";
    //private static final String REF = "ref";
    private static final String VALUE = "value";

    private static final ArrayList<String> LEVELS = new ArrayList<String>();
    static {
      LEVELS.add("debug");
      LEVELS.add("info");
      LEVELS.add("warn");
      LEVELS.add("fatal");
    }

    private Locator locator;
    private IJavaProject project;
    private String file;
    private ArrayList<Error> errors = new ArrayList<Error>();

    //private List appenders = new ArrayList();

    /**
     * Constructs a new instance.
     *
     * @param project The project for this instance.
     * @param file The log4j xml file.
     */
    public Log4jHandler (IJavaProject project, String file)
      throws Exception
    {
      this.project = project;
      this.file = ProjectUtils.getFilePath(project.getProject(), file);
    }

    /**
     * {@inheritDoc}
     * @see org.xml.sax.helpers.DefaultHandler#setDocumentLocator(Locator)
     */
    public void setDocumentLocator (Locator locator)
    {
      super.setDocumentLocator(locator);
      this.locator = locator;
    }

    /**
     * {@inheritDoc}
     * @see org.xml.sax.helpers.DefaultHandler#startElement(String,String,String,Attributes)
     */
    public void startElement (
        String uri, String localName, String qName, Attributes atts)
      throws SAXException
    {
      try{
        /*if(APPENDER.equals(localName)){
          String name = atts.getValue(NAME);
          if(name != null){
            appenders.add(name);
          }
        }else if(APPENDER_REF.equals(localName)){
          String ref = atts.getValue(REF);
          if(ref != null && !appenders.contains(ref)){
            String message =
              Services.getMessage("log4j.appender.name.invalid", ref);
            errors.add(new Error(
                  message, file, locator.getLineNumber(), 1, false
            ));
          }*/
        if(CATEGORY.equals(localName) || LOGGER.equals(localName)){
          String name = atts.getValue(NAME);
          if(name != null){
            IPackageFragment pkg = getPackage(name);
            if(pkg == null){
              IType type = project.findType(name);
              if(type == null || !type.exists()){
                String message =
                  Services.getMessage("log4j.logger.name.invalid", name);
                errors.add(new Error(
                      message, file, locator.getLineNumber(), 1, false
                ));
              }
            }
          }
        }else if(PRIORITY.equals(localName) || LEVEL.equals(localName)){
          String value = atts.getValue(VALUE);
          if(atts.getValue(CLASS) == null && value != null){
            if(!LEVELS.contains(value.trim().toLowerCase())){
              String message =
                Services.getMessage("log4j.level.name.invalid", value);
              errors.add(new Error(
                    message, file, locator.getLineNumber(), 1, false
              ));
            }
          }
        }

        // validate any class attributes.
        String classname = atts.getValue(CLASS);
        if(classname != null){
          IType type = project.findType(classname);
          if(type == null || !type.exists()){
            String message = Services.getMessage("type.not.found",
                project.getElementName(), classname);
            errors.add(new Error(
                  message, file, locator.getLineNumber(), 1, false
            ));
          }
        }
      }catch(Exception e){
        throw new RuntimeException(e);
      }
    }

    /**
     * Gets any errors.
     *
     * @return List of errors.
     */
    public List<Error> getErrors ()
    {
      return errors;
    }

    private IPackageFragment getPackage (String name)
      throws Exception
    {
      IPackageFragmentRoot[] roots = project.getAllPackageFragmentRoots();
      for (int ii = 0; ii < roots.length; ii++){
        IPackageFragment fragment = roots[ii].getPackageFragment(name);
        if(fragment != null && fragment.exists()){
          return fragment;
        }
      }
      return null;
    }
  }
}
