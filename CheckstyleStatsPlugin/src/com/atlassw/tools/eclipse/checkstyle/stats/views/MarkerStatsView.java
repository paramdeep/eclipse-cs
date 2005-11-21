//============================================================================
//
// Copyright (C) 2002-2005  David Schneider, Lars K�dderitzsch, Fabrice Bellingard
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
package com.atlassw.tools.eclipse.checkstyle.stats.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.atlassw.tools.eclipse.checkstyle.stats.Messages;
import com.atlassw.tools.eclipse.checkstyle.stats.StatsCheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.stats.data.MarkerStat;
import com.atlassw.tools.eclipse.checkstyle.stats.data.Stats;
import com.atlassw.tools.eclipse.checkstyle.stats.views.internal.FiltersAction;

/**
 * View that displays statistics about checkstyle markers.
 * 
 * @author Fabrice BELLINGARD
 * @author Lars K�dderitzsch
 */
public class MarkerStatsView extends AbstractStatsView
{

    //
    // constants
    //

    /** The unique view id. */
    public static final String VIEW_ID = "com.atlassw.tools.eclipse.checkstyle.stats.views.MarkerStatsView"; //$NON-NLS-1$

    //
    // attributes
    //

    /** The description label. */
    private Label mDescLabel;

    /** The main composite. */
    private Composite mMainSection;

    /** The current viewer. */
    private TableViewer mCurrentViewer;

    /** Action to show the charts view. */
    private Action mChartAction;

    /** The action to show the detail view. */
    private Action mDrillDownAction;

    /** The action to go back to the master view. */
    private Action mDrillBackAction;

    /** Opens the editor and shows the error in the code. */
    private Action mShowErrorAction;

    /** The current violation category to show in details view. */
    private String mCurrentDetailCategory;

    /** The state if the view is currently drilled down to details. */
    private boolean mIsDrilledDown;

    //
    // methods
    //

    /**
     * {@inheritDoc}
     */
    public void createPartControl(Composite parent)
    {
        try
        {
            super.createPartControl(parent);

            // set up the main layout
            GridLayout layout = new GridLayout(1, false);
            layout.marginWidth = 0;
            layout.marginHeight = 0;
            parent.setLayout(layout);

            // the label
            mDescLabel = new Label(parent, SWT.NONE);
            GridData gridData = new GridData();
            gridData.horizontalAlignment = GridData.FILL;
            gridData.grabExcessHorizontalSpace = true;
            mDescLabel.setLayoutData(gridData);

            // the main section
            mMainSection = new Composite(parent, SWT.NONE);
            layout = new GridLayout(1, false);
            layout.marginWidth = 0;
            layout.marginHeight = 0;
            mMainSection.setLayout(layout);
            mMainSection.setLayoutData(new GridData(GridData.FILL_BOTH));

            // initially create the master viewer
            mCurrentViewer = createMasterView(mMainSection);

            refresh();
            manageActionState();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setFocus()
    {
        mCurrentViewer.getControl().setFocus();
    }

    /**
     * Creates the table viewer for the master view.
     * 
     * @param parent
     *            the parent composite
     * @return the master table viewer
     */
    private TableViewer createMasterView(Composite parent)
    {
        TableViewer masterViewer = new TableViewer(parent, SWT.H_SCROLL
            | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        masterViewer.getControl().setLayoutData(gridData);

        // setup the table columns
        Table table = masterViewer.getTable();
        table.setLinesVisible(true);
        table.setHeaderVisible(true);

        TableColumn idCol = new TableColumn(table, SWT.LEFT, 0);
        idCol.setText(Messages.MarkerStatsView_kindOfErrorColumn);
        idCol.setWidth(400);
        idCol.addSelectionListener(new SorterSelectionListener(masterViewer,
            new NameSorter()));

        TableColumn countCol = new TableColumn(table, SWT.CENTER, 1);
        countCol.setText(Messages.MarkerStatsView_numberOfErrorsColumn);
        countCol.pack();
        countCol.addSelectionListener(new SorterSelectionListener(masterViewer,
            new CountSorter()));

        // set the providers
        masterViewer.setContentProvider(new MasterContentProvider());
        masterViewer.setLabelProvider(new MarkerStatsViewLabelProvider());

        // add selection listener to maintain action state
        masterViewer
            .addSelectionChangedListener(new ISelectionChangedListener()
            {
                public void selectionChanged(SelectionChangedEvent event)
                {
                    manageActionState();
                }
            });

        // hooks the action to double click
        hookDoubleClickAction(mDrillDownAction, masterViewer);

        // and to the context menu too
        ArrayList actionList = new ArrayList(1);
        actionList.add(mDrillDownAction);
        actionList.add(new Separator());
        actionList.add(mChartAction);
        hookContextMenu(actionList, masterViewer);

        return masterViewer;
    }

    /**
     * Creates the table viewer for the detail view.
     * 
     * @param parent
     *            the parent composite
     * @return the detail table viewer
     */
    private TableViewer createDetailView(Composite parent)
    {
        // le tableau
        TableViewer detailViewer = new TableViewer(parent, SWT.H_SCROLL
            | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        detailViewer.getControl().setLayoutData(gridData);

        // setup the table columns
        Table table = detailViewer.getTable();
        table.setLinesVisible(true);
        table.setHeaderVisible(true);

        TableColumn idCol = new TableColumn(table, SWT.LEFT, 0);
        idCol.setText(Messages.DetailStatsView_fileColumn);
        idCol.setWidth(150);
        idCol.addSelectionListener(new SorterSelectionListener(detailViewer,
            new NameSorter()));

        TableColumn folderCol = new TableColumn(table, SWT.LEFT, 1);
        folderCol.setText("In Folder");
        folderCol.setWidth(300);
        folderCol.addSelectionListener(new SorterSelectionListener(
            detailViewer, new NameSorter()));

        TableColumn countCol = new TableColumn(table, SWT.CENTER, 2);
        countCol.setText(Messages.DetailStatsView_lineColumn);
        countCol.pack();

        TableColumn messageCol = new TableColumn(table, SWT.LEFT, 3);
        messageCol.setText(Messages.DetailStatsView_messageColumn);
        messageCol.setWidth(300);

        // set the providers
        detailViewer.setContentProvider(new DetailContentProvider());
        detailViewer.setLabelProvider(new DetailStatsViewLabelProvider());

        // add selection listener to maintain action state
        detailViewer
            .addSelectionChangedListener(new ISelectionChangedListener()
            {
                public void selectionChanged(SelectionChangedEvent event)
                {
                    manageActionState();
                }
            });

        // hooks the action to double click
        hookDoubleClickAction(mShowErrorAction, detailViewer);

        // and to the context menu too
        ArrayList actionList = new ArrayList(1);
        actionList.add(mDrillBackAction);
        actionList.add(mShowErrorAction);
        actionList.add(new Separator());
        actionList.add(mChartAction);
        hookContextMenu(actionList, detailViewer);

        return detailViewer;
    }

    /**
     * {@inheritDoc}
     */
    protected void initMenu(IMenuManager menu)
    {
        menu.add(new FiltersAction(this));
        menu.add(new Separator());
        menu.add(mChartAction);
    }

    /**
     * {@inheritDoc}
     */
    protected void initToolBar(IToolBarManager tbm)
    {
        tbm.add(mDrillBackAction);
        tbm.add(mDrillDownAction);
        tbm.add(new Separator());
        tbm.add(mChartAction);
        tbm.add(new Separator());
        tbm.add(new FiltersAction(this));
    }

    /**
     * {@inheritDoc}
     */
    protected String getViewId()
    {
        return VIEW_ID;
    }

    protected void handleStatsRebuilt()
    {

        if (mCurrentViewer != null && !mCurrentViewer.getTable().isDisposed())
        {

            mCurrentViewer.setInput(getStats());

            // and updates the view description label
            StringBuffer labelBuffer = new StringBuffer(" "); //$NON-NLS-1$
            labelBuffer.append(NLS.bind(
                Messages.StatsViewUtils_checkstyleErrorsCount, new Integer(
                    getStats().getMarkerCount())));

            labelBuffer.append(" - "); //$NON-NLS-1$

            // Collection namesList = StatsViewUtils
            // .computeAnalysedResourceNames(analyserEvent);
            // for (Iterator iter = namesList.iterator();
            // iter.hasNext();)
            // {
            // labelBuffer.append(iter.next());
            // if (iter.hasNext())
            // {
            // labelBuffer.append(", "); //$NON-NLS-1$
            // }
            // }

            mDescLabel.setText(labelBuffer.toString());
            mCurrentViewer.refresh();
        }
    }

    /**
     * See method below.
     * 
     * @see com.atlassw.tools.eclipse.checkstyle.stats.views.AbstractStatsView#makeActions()
     */
    protected void makeActions()
    {
        // Action used to display the pie chart
        mChartAction = new Action()
        {
            public void run()
            {
                try
                {
                    GraphStatsView view = (GraphStatsView) getSite()
                        .getWorkbenchWindow().getActivePage().showView(
                            GraphStatsView.VIEW_ID);
                    // if (view != null)
                    // {
                    // view.statsUpdated(new AnalyserEvent(mStatsToDisplay,
                    // mSelectionToDisplay));
                    // }
                }
                catch (PartInitException e)
                {
                    StatsCheckstylePlugin.log(IStatus.ERROR, NLS.bind(
                        Messages.MarkerStatsView_unableToOpenGraph,
                        GraphStatsView.VIEW_ID), e);
                    // TODO : mettre message d'erreur � l'utilisateur
                }
            }
        };
        mChartAction.setText(Messages.MarkerStatsView_displayChart);
        mChartAction
            .setToolTipText(Messages.MarkerStatsView_displayChartTooltip);
        mChartAction.setImageDescriptor(PlatformUI.getWorkbench()
            .getSharedImages().getImageDescriptor(
                ISharedImages.IMG_OBJS_ERROR_TSK));
        // TODO set another image

        // action used to display the detail of a specific error type
        mDrillDownAction = new Action()
        {
            public void run()
            {
                IStructuredSelection selection = (IStructuredSelection) mCurrentViewer
                    .getSelection();
                if (selection.getFirstElement() instanceof MarkerStat)
                {
                    MarkerStat markerStat = (MarkerStat) selection
                        .getFirstElement();

                    Object currentInput = mCurrentViewer.getInput();

                    mIsDrilledDown = true;
                    mCurrentDetailCategory = markerStat.getIdentifiant();
                    mCurrentViewer.getControl().dispose();

                    mCurrentViewer = createDetailView(mMainSection);
                    mCurrentViewer.setInput(currentInput);

                    mMainSection.layout();
                    mMainSection.redraw();
                    mMainSection.update();

                    manageActionState();
                }
            }
        };
        mDrillDownAction.setText(Messages.MarkerStatsView_showDetails);
        mDrillDownAction
            .setToolTipText(Messages.MarkerStatsView_showDetailsTooltip);
        mDrillDownAction.setImageDescriptor(PlatformUI.getWorkbench()
            .getSharedImages().getImageDescriptor(
                ISharedImages.IMG_TOOL_FORWARD));
        mDrillDownAction.setDisabledImageDescriptor(PlatformUI.getWorkbench()
            .getSharedImages().getImageDescriptor(
                ISharedImages.IMG_TOOL_FORWARD_DISABLED));

        // action used to go back to the master view
        mDrillBackAction = new Action()
        {
            public void run()
            {
                Object currentInput = mCurrentViewer.getInput();

                mIsDrilledDown = false;
                mCurrentDetailCategory = null;
                mCurrentViewer.getControl().dispose();

                mCurrentViewer = createMasterView(mMainSection);
                mCurrentViewer.setInput(currentInput);

                mMainSection.layout();
                mMainSection.redraw();
                mMainSection.update();

                manageActionState();
            }
        };
        mDrillBackAction.setText("Back to Overview");
        mDrillBackAction.setToolTipText("Go back to the master view");
        mDrillBackAction.setImageDescriptor(PlatformUI.getWorkbench()
            .getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_BACK));
        mDrillBackAction.setDisabledImageDescriptor(PlatformUI.getWorkbench()
            .getSharedImages().getImageDescriptor(
                ISharedImages.IMG_TOOL_BACK_DISABLED));

        mShowErrorAction = new Action()
        {
            public void run()
            {
                IStructuredSelection selection = (IStructuredSelection) mCurrentViewer
                    .getSelection();
                if (selection.getFirstElement() instanceof IMarker)
                {
                    IMarker marker = (IMarker) selection.getFirstElement();
                    try
                    {
                        IDE.openEditor(getSite().getPage(), marker);
                    }
                    catch (PartInitException e)
                    {
                        StatsCheckstylePlugin.log(IStatus.ERROR,
                            Messages.DetailStatsView_unableToShowMarker, e);
                        // TODO : mettre message d'erreur � l'utilisateur
                    }
                }
            }
        };
        mShowErrorAction.setText(Messages.DetailStatsView_displayError);
        mShowErrorAction
            .setToolTipText(Messages.DetailStatsView_displayErrorTooltip);
        mShowErrorAction.setImageDescriptor(PlatformUI.getWorkbench()
            .getSharedImages().getImageDescriptor(
                IDE.SharedImages.IMG_OPEN_MARKER));
    }

    /**
     * Helper method to manage the state of the view's actions.
     */
    private void manageActionState()
    {
        mDrillBackAction.setEnabled(mIsDrilledDown);
        mDrillDownAction.setEnabled(!mIsDrilledDown
            && !mCurrentViewer.getSelection().isEmpty());
        mShowErrorAction.setEnabled(mIsDrilledDown
            && !mCurrentViewer.getSelection().isEmpty());
    }

    /**
     * Adds the actions to the tableviewer context menu.
     * 
     * @param actions
     *            a collection of IAction objets
     */
    private void hookContextMenu(final Collection actions,
        StructuredViewer viewer)
    {
        MenuManager menuMgr = new MenuManager();
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener()
        {
            public void menuAboutToShow(IMenuManager manager)
            {
                for (Iterator iter = actions.iterator(); iter.hasNext();)
                {
                    Object item = iter.next();
                    if (item instanceof IContributionItem)
                    {
                        manager.add((IContributionItem) item);
                    }
                    else if (item instanceof IAction)
                    {
                        manager.add((IAction) item);
                    }
                }
                manager.add(new Separator(
                    IWorkbenchActionConstants.MB_ADDITIONS));
            }
        });
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);

        getSite().registerContextMenu(menuMgr, viewer);
    }

    /**
     * Specifies which action will be run when double clicking on the viewer.
     * 
     * @param action
     *            the IAction to add
     */
    private void hookDoubleClickAction(final IAction action,
        StructuredViewer viewer)
    {
        viewer.addDoubleClickListener(new IDoubleClickListener()
        {
            public void doubleClick(DoubleClickEvent event)
            {
                action.run();
            }
        });
    }

    // /**
    // * See method below.
    // *
    // * @see
    // com.atlassw.tools.eclipse.checkstyle.stats.analyser.IAnalyserListener#statsUpdated(com.atlassw.tools.eclipse.checkstyle.stats.analyser.AnalyserEvent)
    // */
    // public void statsUpdated(final AnalyserEvent analyserEvent)
    // {
    // mStatsToDisplay = analyserEvent.getStats();
    // mSelectionToDisplay = analyserEvent.getSelection();
    // Display.getDefault().asyncExec(new Runnable()
    // {
    // /**
    // * Cf. method below.
    // *
    // * @see java.lang.Runnable#run()
    // */
    // public void run()
    // {
    // // update the UI
    // if (getViewer().getContentProvider() != null)
    // {
    // // sets the viewer input
    // getViewer().setInput(mStatsToDisplay.getMarkerStats());
    // // and updates the view description label
    // StringBuffer labelBuffer = new StringBuffer(" "); //$NON-NLS-1$
    // labelBuffer.append(StatsViewUtils
    // .computeMainTitle(analyserEvent));
    // labelBuffer.append(" - "); //$NON-NLS-1$
    // Collection namesList = StatsViewUtils
    // .computeAnalysedResourceNames(analyserEvent);
    // for (Iterator iter = namesList.iterator(); iter.hasNext();)
    // {
    // labelBuffer.append(iter.next());
    // if (iter.hasNext())
    // {
    // labelBuffer.append(", "); //$NON-NLS-1$
    // }
    // }
    // getDescLabel().setText(labelBuffer.toString());
    // }
    // }
    // });
    // }

    /**
     * Content provider for the master table viewer.
     * 
     * @author Lars K�dderitzsch
     */
    private class MasterContentProvider implements IStructuredContentProvider
    {
        private Object[] mCurrentMarkerStats;

        /**
         * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
         */
        public Object[] getElements(Object inputElement)
        {
            if (mCurrentMarkerStats == null)
            {
                // find the marker statistics for the current category
                Stats currentStats = (Stats) inputElement;
                mCurrentMarkerStats = currentStats.getMarkerStats().toArray();
            }

            return mCurrentMarkerStats;
        }

        /**
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        public void dispose()
        {
            mCurrentMarkerStats = null;
        }

        /**
         * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
         *      java.lang.Object, java.lang.Object)
         */
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
        {
            mCurrentMarkerStats = null;
        }
    }

    /**
     * Content provider for the detail table viewer.
     * 
     * @author Lars K�dderitzsch
     */
    private class DetailContentProvider implements IStructuredContentProvider
    {
        private Object[] mCurrentDetails;

        /**
         * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
         */
        public Object[] getElements(Object inputElement)
        {
            if (mCurrentDetails == null)
            {
                // find the marker statistics for the current category
                Stats currentStats = (Stats) inputElement;
                Collection markerStats = currentStats.getMarkerStats();
                Iterator it = markerStats.iterator();
                while (it.hasNext())
                {
                    MarkerStat markerStat = (MarkerStat) it.next();
                    if (markerStat.getIdentifiant().equals(
                        mCurrentDetailCategory))
                    {
                        mCurrentDetails = markerStat.getMarkers().toArray();
                        break;
                    }
                }
            }

            return mCurrentDetails;
        }

        /**
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        public void dispose()
        {
            mCurrentDetails = null;
        }

        /**
         * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
         *      java.lang.Object, java.lang.Object)
         */
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
        {
            mCurrentDetails = null;
        }
    }

    /**
     * Label provider for the master table viewer.
     * 
     * @author Lars K�dderitzsch
     */
    private class MarkerStatsViewLabelProvider extends LabelProvider implements
        ITableLabelProvider
    {
        /**
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
         *      int)
         */
        public String getColumnText(Object obj, int index)
        {
            MarkerStat stat = (MarkerStat) obj;
            String text = null;

            switch (index)
            {
                case 0:
                    text = stat.getIdentifiant();
                    break;
                case 1:
                    text = stat.getCount() + ""; //$NON-NLS-1$
                    break;

                default:
                    text = ""; //$NON-NLS-1$
                    break;
            }

            return text;
        }

        /**
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
         *      int)
         */
        public Image getColumnImage(Object obj, int index)
        {
            return null;
        }
    }

    /**
     * Label provider for the detail table viewer.
     * 
     * @author Lars K�dderitzsch
     */
    private class DetailStatsViewLabelProvider extends LabelProvider implements
        ITableLabelProvider
    {
        /**
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
         *      int)
         */
        public String getColumnText(Object obj, int index)
        {
            IMarker marker = (IMarker) obj;
            String text = null;

            try
            {
                switch (index)
                {
                    case 0:
                        text = marker.getResource().getName();
                        break;
                    case 1:
                        text = marker.getResource().getParent().getFullPath()
                            .toString();
                        break;
                    case 2:
                        text = marker.getAttribute(IMarker.LINE_NUMBER)
                            .toString();
                        break;
                    case 3:
                        text = marker.getAttribute(IMarker.MESSAGE).toString();
                        break;

                    default:
                        text = ""; //$NON-NLS-1$
                        break;
                }
            }
            catch (CoreException e)
            {
                // Can't do anything: let's put a default value
                text = Messages.DetailStatsViewLabelProvider_unknown;
            }

            return text;
        }

        /**
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
         *      int)
         */
        public Image getColumnImage(Object obj, int index)
        {
            return null;
        }
    }

    /**
     * Class used to listen to table viewer column header clicking to sort the
     * different kind of values.
     */
    private static class SorterSelectionListener extends SelectionAdapter
    {

        private StructuredViewer mViewer;

        private ViewerSorter mSorter;

        private ViewerSorter mReverseSorter;

        private ViewerSorter mCurrentSorter;

        /**
         * Constructor.
         * 
         * @param sorter :
         *            the sorter to use
         */
        public SorterSelectionListener(StructuredViewer viewer,
            ViewerSorter sorter)
        {
            mViewer = viewer;
            mSorter = sorter;
            mReverseSorter = new ReverseSorter(sorter);
        }

        public void widgetSelected(SelectionEvent e)
        {
            if (mCurrentSorter == mReverseSorter)
            {
                mCurrentSorter = mSorter;
            }
            else
            {
                mCurrentSorter = mReverseSorter;
            }
            mViewer.setSorter(mCurrentSorter);
        }
    }

    /**
     * A viewer sorter that reverses the result of another sorter.
     * 
     * @author Lars K�dderitzsch
     */
    private static class ReverseSorter extends ViewerSorter
    {
        /** The original sorter. */
        private ViewerSorter mSorter;

        /**
         * Creates a reverse sorter that negates the original sorter.
         * 
         * @param sorter
         *            the original sorter
         */
        public ReverseSorter(ViewerSorter sorter)
        {
            mSorter = sorter;
        }

        /**
         * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer,
         *      java.lang.Object, java.lang.Object)
         */
        public int compare(Viewer viewer, Object e1, Object e2)
        {
            // just negate the result of the original sorter, neat huh? ;-)
            return mSorter.compare(viewer, e1, e2) * -1;
        }
    }

    /**
     * Sorter for Strings.
     */
    private static class NameSorter extends ViewerSorter
    {
        /**
         * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer,
         *      java.lang.Object, java.lang.Object)
         */
        public int compare(Viewer viewer, Object e1, Object e2)
        {
            ITableLabelProvider provider = (ITableLabelProvider) ((TableViewer) viewer)
                .getLabelProvider();

            String label1 = provider.getColumnText(e1, 0);
            String label2 = provider.getColumnText(e2, 0);

            return label1.compareTo(label2);
        }
    }

    /**
     * Sorter for the marker counts.
     */
    private static class CountSorter extends ViewerSorter
    {
        /**
         * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer,
         *      java.lang.Object, java.lang.Object)
         */
        public int compare(Viewer viewer, Object e1, Object e2)
        {
            int count1 = ((MarkerStat) e1).getCount();
            int count2 = ((MarkerStat) e2).getCount();

            return count1 - count2;
        }
    }
}