package dicomux;

import java.io.File;
import java.io.IOException;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.io.DicomInputStream;

/**
 * Controller for Dicomux / Serves as a container for all necessary methods which alter the model
 * @author heidi
 */
public class Controller implements IController {
	/**
	 * holds the model of the application
	 */
	private IModel m_model;
	
	/**
	 * holds the view of the application
	 */
	private IView m_view;
	
	//TODO error handling (check for crap)
	/**
	 * default constructor<br/>
	 * registers the view in the model and vice versa<br>
	 * calls initialize() of the model
	 * @param model
	 * @param view
	 * @see IModel
	 * @see IView
	 */
	public Controller(IModel model, IView view) {
		m_model = model;
		m_view = view;
		
		m_model.registerView(m_view);
		m_view.registerModel(m_model);
		m_view.registerController(this);
		m_model.initialize();
	}
	
	@Override
	public void closeAllWorkspaces() {
		m_model.initialize();
	}
	
	@Override
	public void closeWorkspace() {
		m_model.removeWorkspace(m_view.getActiveWorkspaceId());
	}
	
	@Override
	public void openAbout() {
		for (int i = 0; i < m_model.getWorkspaceCount(); ++i) {
			switch (m_model.getWorkspace(i).getTabState()) {
			case ABOUT: m_model.setWorkspace(i, new TabObject(TabState.ABOUT, true)); return;
			}
		}
		m_model.addWorkspace(new TabObject(TabState.ABOUT));
	}
	
	@Override
	public void openDicomDirectoryDialog() {
		for (int i = 0; i < m_model.getWorkspaceCount(); ++i) {
			switch (m_model.getWorkspace(i).getTabState()) {
			case ERROR_OPEN:
			case FILE_OPEN:
			case DIR_OPEN:
			case WELCOME: m_model.setWorkspace(i, new TabObject(TabState.DIR_OPEN, true)); return;
			}
		}
		m_model.addWorkspace(new TabObject(TabState.DIR_OPEN));
	}
	
	@Override
	public void openDicomFileDialog() {
		for (int i = 0; i < m_model.getWorkspaceCount(); ++i) {
			switch (m_model.getWorkspace(i).getTabState()) {
			case ERROR_OPEN:
			case FILE_OPEN:
			case DIR_OPEN:
			case WELCOME: m_model.setWorkspace(i, new TabObject(TabState.FILE_OPEN, true)); return;
			}
		}
		m_model.addWorkspace(new TabObject(TabState.FILE_OPEN));
	}
	
	@Override
	public void closeApplication() {
		System.exit(0);
	}
	
	//TODO implement good plug-in automatic
	@Override
	public void openDicomFile(String path) {
		// try to open dicom file
		DicomObject dicomObject;
		File fileObject;
		try {
			fileObject = new File(path);
			DicomInputStream din = new DicomInputStream(fileObject);
			dicomObject = din.readDicomObject();
		} catch (IOException e) {
			m_model.setWorkspace(m_view.getActiveWorkspaceId(), new TabObject(TabState.ERROR_OPEN, true));
			e.printStackTrace();
			return;
		}
		
		// attach the dicom file we've just opened to a new TabObject
		TabObject tmp = new TabObject();
		tmp.setDicomObj(dicomObject);
		tmp.setTabActive(true);
		tmp.setName(fileObject.getName());
		
		// choose a suitable plug-in automatically or let the user decide that
		//TODO implement plug-in automatic ------------------------------------------
		tmp.setTabContent(TabState.PLUGIN_CHOOSE);
		// or
		tmp.setTabContent(TabState.PLUGIN_ACTIVE);
		IPlugin chosenPlugin = new RawPlugin(m_view.getLanguage());
		// --------------------------------------------------------------------------
		
		// push the DicomObject to the plug-in and add it to the new TabObject
		chosenPlugin.setData(dicomObject);
		tmp.setPlugin(chosenPlugin);
		
		// push the new TabObject to our workspace
		m_model.setWorkspace(m_view.getActiveWorkspaceId(), tmp);
	}
	
	@Override
	public void setActiveWorkspace(int n) {
		m_model.setActiveWorkspace(n);
	}
	
	//TODO implement
	@Override
	public void openDicomDirectory(String path) {
		
	}
	
	@Override
	public void reinitializeApplicationDialog() {
		for (int i = 0; i < m_model.getWorkspaceCount(); ++i) {
			switch (m_model.getWorkspace(i).getTabState()) {
			case WELCOME:
			case RESTART: m_model.setWorkspace(i, new TabObject(TabState.RESTART, true)); return;
			}
		}
		m_model.addWorkspace(new TabObject(TabState.RESTART));
	}
}
