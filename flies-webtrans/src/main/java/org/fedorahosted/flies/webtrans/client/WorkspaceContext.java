package org.fedorahosted.flies.webtrans.client;

import net.customware.gwt.dispatch.client.DispatchAsync;

import org.fedorahosted.flies.gwt.model.LocaleId;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;
import org.fedorahosted.flies.gwt.rpc.GetWorkspaceContext;
import org.fedorahosted.flies.gwt.rpc.GetWorkspaceContextResult;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class WorkspaceContext {
	private final ProjectContainerId projectContainerId;
	private final LocaleId localeId;

	private String workspaceName;
	private String localeName;

	private final DispatchAsync dispatcher;
	
	@Inject
	public WorkspaceContext(DispatchAsync dispatcher) {
		this.projectContainerId = findProjectContainerId();
		this.localeId = findLocaleId();
		this.dispatcher = dispatcher;
		validateWorkspace();
	}

	private static LocaleId findLocaleId() {
		String localeId = Window.Location.getParameter("localeId");
		return localeId == null ? null : new LocaleId(localeId);
	}
	
	private static ProjectContainerId findProjectContainerId() {
		String projContainerId = Window.Location.getParameter("projContainerId");
		if(projContainerId == null)
			return null;
		try{
			int id = Integer.parseInt(projContainerId);
			return new ProjectContainerId(id);
		}
		catch(NumberFormatException nfe){
			return null;
		}
	}
	
	private void validateWorkspace() {
		dispatcher.execute(new GetWorkspaceContext(projectContainerId, localeId), new AsyncCallback<GetWorkspaceContextResult>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			@Override
			public void onSuccess(GetWorkspaceContextResult result) {
				setWorkspaceName(result.getWorkspaceName());
				setLocaleName(result.getLocaleName());
			}
		});
	}
	
	public ProjectContainerId getProjectContainerId() {
		return projectContainerId;
	}

	public LocaleId getLocaleId() {
		return localeId; 
	}
	
	public boolean isValid() {
		//return workspaceName != null && localeName != null;
		return true;
	}
	
	private void setWorkspaceName(String workspaceName) {
		this.workspaceName = workspaceName;
	}
	
	public String getWorkspaceName() {
		return workspaceName;
	}
	
	private void setLocaleName(String localeName) {
		this.localeName = localeName;
	}
	
	public String getLocaleName() {
		return localeName;
	}
}