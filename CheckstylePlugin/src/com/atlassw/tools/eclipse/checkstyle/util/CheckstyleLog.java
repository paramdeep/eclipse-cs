//============================================================================
//
// Copyright (C) 2002-2004  David Schneider
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
//============================================================================

package com.atlassw.tools.eclipse.checkstyle.util;

//=================================================
// Imports from java namespace
//=================================================

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;

//=================================================
// Imports from org namespace
//=================================================
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Logging utility for the Checkstyle plug-in.
 */
public final class CheckstyleLog
{
    //=================================================
    // Public static final variables.
    //=================================================

    //=================================================
    // Static class variables.
    //=================================================

    private static ILog         sLog;

    private static final String NEWLINE   = System.getProperty("line.separator");

    private static final String ADDED_MSG = "See your <workspace>/.metadata/.log file for additional details.";

    //=================================================
    // Instance member variables.
    //=================================================

    //=================================================
    // Constructors & finalizer.
    //=================================================

    private CheckstyleLog()
    {}

    static
    {
        sLog = CheckstylePlugin.getDefault().getLog();
    }

    //=================================================
    // Methods.
    //=================================================

    /**
     * Log an error message.
     * 
     * @param message Log message.
     */
    public static void error(String message)
    {
        error(message, null);
    }

    /**
     * Log an error message.
     * 
     * @param message Log message.
     * 
     * @param exception Ecxeption that caused the error.
     */
    public static void error(String message, Throwable exception)
    {
        Status status = new Status(Status.ERROR, CheckstylePlugin.PLUGIN_ID, Status.OK,
                "Checkstyle: " + message, exception);
        sLog.log(status);
    }

    /**
     * Log a warning message.
     * 
     * @param message Log message.
     */
    public static void warning(String message)
    {
        warning(message, null);
    }

    /**
     * Log a warning message.
     * 
     * @param message Log message.
     * 
     * @param exception Ecxeption that caused the error.
     */
    public static void warning(String message, Throwable exception)
    {
        Status status = new Status(Status.WARNING, CheckstylePlugin.PLUGIN_ID, Status.OK,
                "Checkstyle: " + message, exception);
        sLog.log(status);
    }

    /**
     * Log an information message.
     * 
     * @param message Log message.
     */
    public static void info(String message)
    {
        Status status = new Status(Status.INFO, CheckstylePlugin.PLUGIN_ID, Status.OK,
                "Checkstyle: " + message, null);
        sLog.log(status);
    }

    /**
     * Displays a simple error dialog indicating there was a Checkstyle internal
     * error.
     * 
     * @param shell Shell the use for the dialog.
     */
    public static void internalErrorDialog(Shell shell)
    {
        errorDialog(shell, "A Checkstyle internal error occured.");
    }

    /**
     * Displays a simple error dialog indicating there was a Checkstyle internal
     * error.
     */
    public static void internalErrorDialog()
    {
        errorDialog("A Checkstyle internal error occured.");
    }

    /**
     * Displays a simple error dialog indicating there was a Checkstyle internal
     * error.
     * 
     * @param msg Message to display.
     */
    public static void errorDialog(String msg)
    {
        errorDialog(getShell(), msg);
    }

    /**
     * Displays a simple error dialog indicating there was a Checkstyle internal
     * error.
     * 
     * @param shell Shell to use for the dialog.
     * 
     * @param msg Message to display.
     */
    public static void errorDialog(Shell shell, String msg)
    {
        if (shell != null)
        {
            String logMsg = msg + NEWLINE + ADDED_MSG;
            MessageDialog.openError(shell, "Checkstyle Error", logMsg);
        }
    }

    // ad, 7.Jan.2004, Bug #872279
    /**
     * ad, 7.Jan.2004, Bug #872279 Displays a simple yes/no dialog.
     * <p>
     * 
     * @param shell the Shell object
     * @param msg Message to display.
     * @return boolean true if the user pressed OK. false if the user cancelled
     *         the dialog.
     */
    public static boolean questionDialog(Shell shell, String msg)
    {
        if (shell != null)
        {
            String logMsg = msg;
            return MessageDialog.openQuestion(shell, "Checkstyle Question", logMsg);
        }
        else
        {
            return false;
        }
    }

    // ad, 7.Jan.2004, Bug #872279
    // end change

    private static Shell getShell()
    {
        Shell shell = null;

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null)
        {
            shell = window.getShell();
        }

        return shell;
    }
}