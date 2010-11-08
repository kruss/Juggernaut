package ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;


import launch.LaunchConfig;
import core.Application;
import core.ConfigStore;
import core.IChangeListener;

public class ConfigPanelLaunch extends JPanel implements IChangeListener {

	private static final long serialVersionUID = 1L;

	private ConfigPanel parent;
	private OptionEditor editor;
	
	public ConfigPanelLaunch(ConfigPanel parent){
		
		this.parent = parent;
		editor = new OptionEditor();

		setLayout(new BorderLayout());
		add(editor, BorderLayout.NORTH);
		
		parent.addListener(this);
		editor.addListener(this);
		initUI();
	}

	@Override
	public void changed(Object object) {
		
		if(object instanceof ConfigPanel){
			initUI();
		}
		
		if(object instanceof OptionEditor){
			ConfigStore store = Application.getInstance().getConfigStore();
			parent.getLaunchConfig().setDirty(true);
			store.notifyListeners();
		}
	}

	private void initUI() {

		LaunchConfig config = parent.getLaunchConfig();
		if(config != null){
			editor.setOptionContainer(config.getOptionContainer());
		}else{
			editor.setOptionContainer(null);
		}
	}
}
