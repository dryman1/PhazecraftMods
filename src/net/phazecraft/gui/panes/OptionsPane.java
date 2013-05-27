package net.phazecraft.gui.panes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

import net.phazecraft.data.Settings;
import net.phazecraft.gui.ChooseDir;
import net.phazecraft.gui.LaunchFrame;
import net.phazecraft.gui.dialogs.AdvancedOptionsDialog;
import net.phazecraft.locale.I18N;
import net.phazecraft.log.Logger;
import net.phazecraft.util.OSUtils;

@SuppressWarnings("serial")
public class OptionsPane extends JPanel implements ILauncherPane {
	private JToggleButton tglbtnForceUpdate;
	private JButton installBrowseBtn, advancedOptionsBtn;
	private JLabel lblInstallFolder, lblRamMaximum, lblLocale, currentRam, minecraftSize, lblX;
	private JSlider ramMaximum;
	private JComboBox locale;
	private JButton forceClear, clearInstall;
	private JTextField installFolderTextField;
	private JCheckBox chckbxShowConsole, keepLauncherOpen;
	private final Settings settings;

	private FocusListener settingsChangeListener = new FocusListener() {
		@Override
		public void focusLost(FocusEvent e) {
			saveSettingsInto(settings);
		}

		@Override
		public void focusGained(FocusEvent e) {
		}
	};

	public OptionsPane(Settings settings) {
		this.settings = settings;
		setBorder(new EmptyBorder(5, 5, 5, 5));

		installBrowseBtn = new JButton("...");
		installBrowseBtn.setBounds(786, 11, 49, 28);
		installBrowseBtn.addActionListener(new ChooseDir(this));
		setLayout(null);
		add(installBrowseBtn);

		lblInstallFolder = new JLabel(I18N.getLocaleString("INSTALL_FOLDER"));
		lblInstallFolder.setBounds(10, 11, 127, 28);
		add(lblInstallFolder);

		installFolderTextField = new JTextField();
		installFolderTextField.setBounds(147, 11, 629, 28);
		installFolderTextField.addFocusListener(settingsChangeListener);
		installFolderTextField.setColumns(10);
		installFolderTextField.setText(settings.getInstallPath());
		add(installFolderTextField);

		tglbtnForceUpdate = new JToggleButton(I18N.getLocaleString("FORCE_UPDATE"));
		tglbtnForceUpdate.setBounds(147, 48, 629, 29);
		tglbtnForceUpdate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				saveSettingsInto(OptionsPane.this.settings);
			}
		});

		forceClear = new JButton("Forec Clear All Mods");
		forceClear.setBounds(147, 315, 629, 29);
		forceClear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int input = JOptionPane.showConfirmDialog(null, "Clear all mods\nThis is the point of no return", "Confirm Action", JOptionPane.YES_NO_OPTION);
				if (input == JOptionPane.YES_OPTION) {
					try {
						org.apache.commons.io.FileUtils.deleteDirectory(new File(OSUtils.getDynamicStorageLocation(), "mods"));
					} catch (IOException e1) {
						Logger.logError("Could not clean Mods Folder", e1);
						JOptionPane.showMessageDialog(null, "The clear mods operation finished with some warnings\nNothing to serious", "Error", JOptionPane.WARNING_MESSAGE);
					} catch (Exception e2) {
						Logger.logError("Could not clean Mods Folder", e2);
						JOptionPane.showMessageDialog(null, "The clear mods operation failed epicley", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		add(forceClear);

		clearInstall = new JButton("Force Clear The Install");
		clearInstall.setBounds(147, 355, 629, 29);
		clearInstall.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int input = JOptionPane.showConfirmDialog(null, "Clear the install?  This will save disk space, but delete all saves\nThis is the point of no return", "Confirm Action", JOptionPane.YES_NO_OPTION);
				if (input == JOptionPane.YES_OPTION) {
					try {
						String[] files = new String[new File(OSUtils.getDefInstallPath()).list().length];
						for (int i1 = 0; i1 < files.length; i1++)
							files[i1] = new File(OSUtils.getDefInstallPath()).list()[i1];
						for (int i = 0; i < new File(OSUtils.getDefInstallPath()).list().length; i++)
							org.apache.commons.io.FileUtils.deleteDirectory(new File(OSUtils.getDefInstallPath() + "/" + files[i]));
					} catch (IOException e1) {
						Logger.logError("Could not clean Mods Folder", e1);
						JOptionPane.showMessageDialog(null, "The clear install operation finished with some warnings\nNothing to serious", "Error", JOptionPane.WARNING_MESSAGE);
					} catch (Exception e2) {
						Logger.logError("Could not clean Mods Folder", e2);
						JOptionPane.showMessageDialog(null, "The clear install operation failed epicley", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		add(clearInstall);

		tglbtnForceUpdate.getModel().setPressed(settings.getForceUpdate());
		add(tglbtnForceUpdate);

		currentRam = new JLabel();
		currentRam.setBounds(427, 95, 85, 25);
		long ram = 0;
		OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
		Method m;
		try {
			m = operatingSystemMXBean.getClass().getDeclaredMethod("getTotalPhysicalMemorySize");
			m.setAccessible(true);
			Object value = m.invoke(operatingSystemMXBean);
			if (value != null) {
				ram = Long.valueOf(value.toString()) / 1024 / 1024;
			} else {
				Logger.logWarn("Could not get RAM Value");
				ram = 8192;
			}
		} catch (Exception e) {
			Logger.logError(e.getMessage(), e);
		}

		ramMaximum = new JSlider();
		ramMaximum.setBounds(190, 95, 222, 25);
		ramMaximum.setSnapToTicks(true);
		ramMaximum.setMajorTickSpacing(256);
		ramMaximum.setMinorTickSpacing(256);
		ramMaximum.setMinimum(256);
		String vmType = System.getProperty("sun.arch.data.model");
		if (vmType != null) {
			if (vmType.equals("64")) {
				ramMaximum.setMaximum((int) ram);
			} else if (vmType.equals("32")) {
				if (ram < 1024) {
					ramMaximum.setMaximum((int) ram);
				} else {
					ramMaximum.setMaximum(1024);
				}
			}
		}
		int ramMax = (Integer.parseInt(settings.getRamMax()) > ramMaximum.getMaximum()) ? ramMaximum.getMaximum() : Integer.parseInt(settings.getRamMax());
		ramMaximum.setValue(ramMax);
		currentRam.setText(getAmount());
		ramMaximum.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				currentRam.setText(getAmount());
			}
		});
		ramMaximum.addFocusListener(settingsChangeListener);

		lblRamMaximum = new JLabel(I18N.getLocaleString("RAM_MAX"));
		lblRamMaximum.setBounds(10, 95, 195, 25);
		add(lblRamMaximum);
		add(ramMaximum);
		add(currentRam);

		String[] locales = new String[I18N.localeIndices.size()];
		for (Map.Entry<Integer, String> entry : I18N.localeIndices.entrySet()) {
			Logger.logInfo("[i18n] Added " + entry.getKey().toString() + " " + entry.getValue() + " to options pane");
			locales[entry.getKey()] = I18N.localeFiles.get(entry.getValue());
		}
		locale = new JComboBox(locales);
		locale.setBounds(190, 130, 222, 25);
		locale.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				I18N.setLocale(I18N.localeIndices.get(locale.getSelectedIndex()));
				if (LaunchFrame.getInstance() != null) {
					LaunchFrame.getInstance().updateLocale();
				}
			}
		});
		locale.addFocusListener(settingsChangeListener);
		locale.setSelectedItem(I18N.localeFiles.get(settings.getLocale()));

		lblLocale = new JLabel(I18N.getLocaleString("LANGUAGE"));
		lblLocale.setBounds(10, 130, 195, 25);
		add(lblLocale);
		add(locale);

		chckbxShowConsole = new JCheckBox(I18N.getLocaleString("SHOW_CONSOLE"));
		chckbxShowConsole.addFocusListener(settingsChangeListener);
		chckbxShowConsole.setSelected(settings.getConsoleActive());
		chckbxShowConsole.setBounds(550, 95, 183, 25);
		add(chckbxShowConsole);

		keepLauncherOpen = new JCheckBox(I18N.getLocaleString("REOPEN_LAUNCHER"));
		keepLauncherOpen.setBounds(550, 130, 300, 25);
		keepLauncherOpen.setSelected(settings.getKeepLauncherOpen());
		keepLauncherOpen.addFocusListener(settingsChangeListener);
		add(keepLauncherOpen);

		advancedOptionsBtn = new JButton(I18N.getLocaleString("ADVANCED_OPTIONS"));
		advancedOptionsBtn.setBounds(147, 275, 629, 29);
		advancedOptionsBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				AdvancedOptionsDialog aod = new AdvancedOptionsDialog();
				aod.setVisible(true);
			}
		});
		advancedOptionsBtn.getModel().setPressed(settings.getForceUpdate());
		add(advancedOptionsBtn);
	}

	public void setInstallFolderText(String text) {
		installFolderTextField.setText(text);
		saveSettingsInto(settings);
	}

	public void saveSettingsInto(Settings settings) {
		settings.setInstallPath(installFolderTextField.getText());
		settings.setForceUpdate(tglbtnForceUpdate.isSelected());
		settings.setRamMax(String.valueOf(ramMaximum.getValue()));
		settings.setLocale(I18N.localeIndices.get(locale.getSelectedIndex()));
		settings.setConsoleActive(chckbxShowConsole.isSelected());
		settings.setKeepLauncherOpen(keepLauncherOpen.isSelected());
		settings.save();
	}

	public void updateLocale() {
		lblInstallFolder.setText(I18N.getLocaleString("INSTALL_FOLDER"));
		tglbtnForceUpdate.setText(I18N.getLocaleString("FORCE_UPDATE"));
		lblRamMaximum.setText(I18N.getLocaleString("RAM_MAX"));
		lblLocale.setText(I18N.getLocaleString("LANGUAGE"));
	}

	private String getAmount() {
		int ramMax = ramMaximum.getValue();
		return (ramMax >= 1024) ? Math.round((ramMax / 256) / 4) + "." + (((ramMax / 256) % 4) * 25) + " GB" : ramMax + " MB";
	}

	@Override
	public void onVisible() {
	}
}
