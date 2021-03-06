/**
 *
 */
package org.goko.gcode.rs274ngcv3.ui.workspace.uiprovider;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.goko.core.common.exception.GkException;
import org.goko.core.gcode.element.IGCodeProvider;
import org.goko.core.gcode.rs274ngcv3.IRS274NGCService;
import org.goko.core.gcode.rs274ngcv3.element.GCodeProvider;
import org.goko.core.gcode.rs274ngcv3.element.IModifier;
import org.goko.core.gcode.service.IExecutionService;
import org.goko.core.log.GkLog;
import org.goko.core.workspace.bean.IPropertiesPanel;
import org.goko.core.workspace.bean.ProjectContainerUiProvider;
import org.goko.core.workspace.service.IWorkspaceService;
import org.goko.core.workspace.service.IWorkspaceUIService;
import org.goko.gcode.rs274ngcv3.ui.workspace.IRS274WorkspaceService;
import org.goko.gcode.rs274ngcv3.ui.workspace.uiprovider.menu.gcodeprovider.AddExecutionQueueAction;
import org.goko.gcode.rs274ngcv3.ui.workspace.uiprovider.menu.gcodeprovider.DeleteGCodeProviderAction;
import org.goko.gcode.rs274ngcv3.ui.workspace.uiprovider.menu.gcodeprovider.IGCodeProviderContributionItem;
import org.goko.gcode.rs274ngcv3.ui.workspace.uiprovider.menu.gcodeprovider.ModifierSubMenu;
import org.goko.gcode.rs274ngcv3.ui.workspace.uiprovider.menu.gcodeprovider.ReloadGCodeProviderAction;
import org.goko.gcode.rs274ngcv3.ui.workspace.uiprovider.menu.gcoderepository.AddAllGCodeInQueueAction;
import org.goko.gcode.rs274ngcv3.ui.workspace.uiprovider.menu.modifier.DeleteModifierAction;
import org.goko.gcode.rs274ngcv3.ui.workspace.uiprovider.menu.modifier.EnableDisableAction;
import org.goko.gcode.rs274ngcv3.ui.workspace.uiprovider.menu.modifier.ModifierMoveDownAction;
import org.goko.gcode.rs274ngcv3.ui.workspace.uiprovider.menu.modifier.ModifierMoveUpAction;
import org.goko.gcode.rs274ngcv3.ui.workspace.uiprovider.panel.IModifierPropertiesPanel;
import org.osgi.service.event.EventAdmin;

/**
 * @author PsyKo
 * @date 31 oct. 2015
 */
public class GCodeContainerUiProvider extends ProjectContainerUiProvider {
	/** LOG */
	private static final GkLog LOG = GkLog.getLogger(GCodeContainerUiProvider.class);
	/** GCode service */
	private IRS274NGCService rs274Service;
	private IRS274WorkspaceService rs274WorkspaceService;
	private IWorkspaceService workspaceService;
	private IExecutionService<?, ?> executionService;
	private IStyledLabelProvider labelProvider;
	private EventAdmin eventAdmin;
	private List<IGCodeProviderContributionItem> lstGCodeProviderContributionItem;
	private IWorkspaceUIService workspaceUIService;
	
	/**
	 * @param rs274Service
	 * @param type
	 */
	public GCodeContainerUiProvider() {		
		super("GCodeContainerUiProvider", 10);
		this.labelProvider = new GCodeContainerLabelProvider();
		this.lstGCodeProviderContributionItem = new CopyOnWriteArrayList<IGCodeProviderContributionItem>();
		LOG.info("Creating GCodeContainerUiProvider");
	}

	/** (inheritDoc)
	 * @see org.goko.core.workspace.bean.ProjectContainerUiProvider#providesLabelFor(java.lang.Object)
	 */
	@Override
	public boolean providesLabelFor(Object content) throws GkException {
		return this.equals(content)
			|| (content instanceof IGCodeProvider)
			|| (content instanceof IModifier);
	}

	/** (inheritDoc)
	 * @see org.goko.core.workspace.bean.ProjectContainerUiProvider#getStyledText(java.lang.Object)
	 */
	@Override
	public StyledString getStyledText(Object element) {
		return labelProvider.getStyledText(element);
	}

	/** (inheritDoc)
	 * @see org.goko.core.workspace.bean.ProjectContainerUiProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element) {
		return labelProvider.getImage(element);
	}

	/** (inheritDoc)
	 * @see org.goko.core.workspace.bean.ProjectContainerUiProvider#providesContentFor(java.lang.Object)
	 */
	@Override
	public boolean providesContentFor(Object content) throws GkException {
		return this.equals(content) || content instanceof IGCodeProvider;
	}

	/** (inheritDoc)
	 * @see org.goko.core.workspace.bean.ProjectContainerUiProvider#hasChildren(java.lang.Object)
	 */
	@Override
	public boolean hasChildren(Object content) throws GkException {
		if(content instanceof IGCodeProvider){
			List<IModifier<GCodeProvider>> lst = rs274Service.getModifierByGCodeProvider(((IGCodeProvider) content).getId());
			return CollectionUtils.isNotEmpty(lst);
		}else if(this.equals(content)){
			return CollectionUtils.isNotEmpty(rs274Service.getGCodeProvider());
		}
		return false;
	}


	/** (inheritDoc)
	 * @see org.goko.core.workspace.bean.ProjectContainerUiProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object content) throws GkException {
		if(content instanceof IGCodeProvider){
			return rs274Service.getModifierByGCodeProvider(((IGCodeProvider) content).getId()).toArray();
		}else if(this.equals(content)){
			return rs274Service.getGCodeProvider().toArray();
		}
		return null;
	}

	/** (inheritDoc)
	 * @see org.goko.core.workspace.bean.ProjectContainerUiProvider#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object content) throws GkException {
		if(content instanceof IGCodeProvider){
			return this;
		}else if(content instanceof IModifier){
			IModifier<?> modifier = (IModifier<?>)content;
			return rs274Service.getGCodeProvider(modifier.getIdGCodeProvider());
		}
		return null;
	}

	/** (inheritDoc)
	 * @see org.goko.core.workspace.bean.ProjectContainerUiProvider#providesMenuFor(java.lang.Object)
	 */
	@Override
	public boolean providesMenuFor(ISelection selection) throws GkException {
		IStructuredSelection strSelection = (IStructuredSelection) selection;
		Object content = strSelection.getFirstElement();
		return this.equals(content)
			|| (content instanceof IGCodeProvider)
			|| (content instanceof IModifier);
	}

	/** (inheritDoc)
	 * @see org.goko.core.workspace.bean.ProjectContainerUiProvider#createMenuFor(org.eclipse.jface.action.IMenuManager, java.lang.Object)
	 */
	@Override
	public void createMenuFor(IMenuManager contextMenu, ISelection selection) throws GkException {
		IStructuredSelection strSelection = (IStructuredSelection) selection;
		Object content = strSelection.getFirstElement();

		if(this.equals(content)){
			createMenuForGCodeRepository(contextMenu);
		}else if(content instanceof IGCodeProvider){
			createMenuForGCodeProvider(contextMenu, (IGCodeProvider)content);
		}else if(content instanceof IModifier<?>){
			createMenuForGCodeModifier(contextMenu, (IModifier<?>)content);
		}
	}

	/**
	 * Creates the menu for the GCode repository node of the tree
	 * @param contextMenu the target context menu
	 */
	private void createMenuForGCodeRepository(IMenuManager contextMenu) {
		contextMenu.add(new AddAllGCodeInQueueAction(executionService, rs274Service));
	}

	/**
	 * Creates the menu for a GCode modifier node of the tree
	 * @param contextMenu the target context menu
	 */
	private void createMenuForGCodeModifier(IMenuManager contextMenu, IModifier<?> modifier) {
		contextMenu.add(new EnableDisableAction(rs274Service, modifier.getId()));
		contextMenu.add(new ModifierMoveUpAction(rs274Service, modifier.getId()));
		contextMenu.add(new ModifierMoveDownAction(rs274Service, modifier.getId()));
		contextMenu.add(new Separator());
		contextMenu.add(new DeleteModifierAction(rs274Service, modifier.getId()));
	}

	/**
	 * Creates the menu for a GCode provider node of the tree
	 * @param contextMenu the target context menu
	 */
	protected void createMenuForGCodeProvider(IMenuManager contextMenu, final IGCodeProvider content) throws GkException {
		// Submenu for a specific user
        MenuManager subMenu = new ModifierSubMenu(rs274Service, rs274WorkspaceService, content.getId());
        

        contextMenu.add(new AddExecutionQueueAction(rs274Service, executionService, content.getId()));
        contextMenu.add(new Separator());
        contextMenu.add(new ReloadGCodeProviderAction(rs274Service, content.getId()));
      //  contextMenu.add(new ExternalEditAction(rs274Service, workspaceService, content.getId()));
        contextMenu.add(subMenu);
        if(CollectionUtils.isNotEmpty(lstGCodeProviderContributionItem)){
        	contextMenu.add(new Separator());
	        for (IGCodeProviderContributionItem contributionItem : lstGCodeProviderContributionItem) {
	        	contextMenu.add(contributionItem.getItem(content));
			}
        }
        contextMenu.add(new Separator());
        contextMenu.add(new DeleteGCodeProviderAction(rs274Service, content.getId()));
	}


	/** (inheritDoc)
	 * @see org.goko.core.workspace.bean.ProjectContainerUiProvider#providesConfigurationPanelFor(java.lang.Object)
	 */
	@Override
	public boolean providesConfigurationPanelFor(ISelection selection) throws GkException {
		IStructuredSelection strSelection = (IStructuredSelection) selection;
		Object content = strSelection.getFirstElement();

		if(content instanceof IModifier<?>){
			IModifier<?> iModifier = (IModifier<?>) content;

			List<IModifierUiProvider<?>> lstBuilders = rs274WorkspaceService.getModifierBuilder();
			if(CollectionUtils.isNotEmpty(lstBuilders)){
				for (IModifierUiProvider<?> iModifierUiProvider : lstBuilders) {
					if(iModifierUiProvider.providesConfigurationPanelFor(iModifier)){
						return true;
					}
				}
			}
		}
		return false;
	}

	/** (inheritDoc)
	 * @see org.goko.core.workspace.bean.ProjectContainerUiProvider#createConfigurationPanelFor(org.eclipse.swt.widgets.Composite, java.lang.Object)
	 */
	@Override
	public IPropertiesPanel createConfigurationPanelFor(Composite parent, ISelection selection) throws GkException {
		IStructuredSelection strSelection = (IStructuredSelection) selection;
		Object content = strSelection.getFirstElement();

		if(content instanceof IModifier<?>){
			IModifier<?> iModifier = (IModifier<?>) content;

			List<IModifierUiProvider<?>> lstBuilders = rs274WorkspaceService.getModifierBuilder();
			if(CollectionUtils.isNotEmpty(lstBuilders)){
				for (IModifierUiProvider<?> iModifierUiProvider : lstBuilders) {
					if(iModifierUiProvider.providesConfigurationPanelFor(iModifier)){
						IModifierPropertiesPanel<?> panel = iModifierUiProvider.createConfigurationPanelFor(parent, iModifier);
						panel.initializeFromModifier();
						return panel;
					}
				}
			}
		}
		return null;
	}

	/**
	 * @return the rs274Service
	 */
	public IRS274NGCService getRs274Service() {
		return rs274Service;
	}

	/**
	 * @param rs274Service the rs274Service to set
	 */
	public void setRs274Service(IRS274NGCService rs274Service) {
		this.rs274Service = rs274Service;
	}

	/**
	 * @return the rs274WorkspaceService
	 */
	public IRS274WorkspaceService getRs274WorkspaceService() {
		return rs274WorkspaceService;
	}

	/**
	 * @param rs274WorkspaceService the rs274WorkspaceService to set
	 */
	public void setRs274WorkspaceService(IRS274WorkspaceService rs274WorkspaceService) {
		this.rs274WorkspaceService = rs274WorkspaceService;		
	}

	/**
	 * @return the workspaceService
	 */
	public IWorkspaceService getWorkspaceService() {
		return workspaceService;
	}

	/**
	 * @param workspaceService the workspaceService to set
	 */
	public void setWorkspaceService(IWorkspaceService workspaceService) {
		this.workspaceService = workspaceService;
	}

	/**
	 * @return the executionService
	 */
	public IExecutionService<?, ?> getExecutionService() {
		return executionService;
	}

	/**
	 * @param executionService the executionService to set
	 */
	public void setExecutionService(IExecutionService<?, ?> executionService) {
		this.executionService = executionService;
	}

	/**
	 * @return the eventAdmin
	 */
	public EventAdmin getEventAdmin() {
		return eventAdmin;
	}

	/**
	 * @param eventAdmin the eventAdmin to set
	 */
	public void setEventAdmin(EventAdmin eventAdmin) {
		this.eventAdmin = eventAdmin;
	}

	public void addGCodeProviderContributionItem(IGCodeProviderContributionItem contributionItem){
		this.lstGCodeProviderContributionItem.add(contributionItem);
	}

	/**
	 * @return the workspaceUIService
	 */
	public IWorkspaceUIService getWorkspaceUIService() {
		return workspaceUIService;
	}

	/**
	 * @param workspaceUIService the workspaceUIService to set
	 * @throws GkException 
	 */
	public void setWorkspaceUIService(IWorkspaceUIService workspaceUIService) throws GkException {
		this.workspaceUIService = workspaceUIService;
		this.workspaceUIService.addProjectContainerUiProvider(this);
	}

}
