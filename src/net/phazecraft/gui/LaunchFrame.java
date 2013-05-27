package net.phazecraft.gui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.WindowConstants;

import net.phazecraft.data.LoginResponse;
import net.phazecraft.data.Settings;
import net.phazecraft.data.UserManager;
import net.phazecraft.gui.dialogs.InstallDirectoryDialog;
import net.phazecraft.gui.dialogs.SplashScreen;
import net.phazecraft.gui.panes.OptionsPane;
import net.phazecraft.locale.I18N;
import net.phazecraft.locale.I18N.Locale;
import net.phazecraft.log.Logger;
import net.phazecraft.tools.ModManager;
import net.phazecraft.tracking.AnalyticsConfigData;
import net.phazecraft.tracking.JGoogleAnalyticsTracker;
import net.phazecraft.tracking.JGoogleAnalyticsTracker.GoogleAnalyticsVersion;
import net.phazecraft.util.DownloadUtils;
import net.phazecraft.util.ErrorUtils;
import net.phazecraft.util.FileUtils;
import net.phazecraft.util.OSUtils;
import net.phazecraft.util.StyleUtil;
import net.phazecraft.util.TrackerUtils;
import net.phazecraft.workers.GameUpdateWorker;

@SuppressWarnings("static-access")
public class LaunchFrame extends JFrame implements ActionListener, KeyListener,
		MouseWheelListener, MouseListener, MouseMotionListener {

	private static final long serialVersionUID = 1L;

	static LaunchFrame frame;

	private LoginResponse RESPONSE;

	private JButton launch = new LiteButton("Install");
	private JButton uninstall = new LiteButton("Uninstall");
	private JButton website = new LiteButton("Website");
	private JButton edit = new JButton();
	private JButton tpInstall = new JButton();
	private JButton serverMap = new JButton();
	private JButton mapInstall = new JButton();
	private JButton serverbutton = new JButton();
	private JButton donate = new JButton();
	private static String[] dropdown_ = { "Select Profile", "Create Profile" };
	@SuppressWarnings("rawtypes")
	private static JComboBox users, tpInstallLocation, mapInstallLocation;
	private static LaunchFrame instance = null;
	private static String version = "1.2.3";

	public final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);

	protected static UserManager userManager;

	public OptionsPane optionsPane;

	private LiteTextBox name;
	private LitePasswordBox pass;
	private JButton login;
	private JCheckBox inject;

	public int mouseX = 0;
	public int mouseY = 0;

	public static int buildNumber = 123;
	public static boolean noConfig = false;
	public static LauncherConsole con;
	public static String tempPass = "";
	public static Panes currentPane = Panes.MODPACK;
	public static JGoogleAnalyticsTracker tracker = new JGoogleAnalyticsTracker(
			new AnalyticsConfigData("UA-39727539-1"),
			GoogleAnalyticsVersion.V_4_7_2);
	private static final Color TRANSPARENT = new Color(45, 45, 45, 160);

	public static JFrame modsFrame;
	public static JFrame textureFrame;
	public static JFrame mapsFrame;
	public static JFrame optionsFrame;

	public static boolean isAuth = true;
	public static boolean isUpdate = true;

	private static SplashScreen splash;


	TransparentButton exit;

	public static final String FORGENAME = "MinecraftForge.zip";

	public static String mod = "Biomes O' Plenty";
	public static String modName = "Biomes.zip";
	public static String modVersion = "1_5_2";
	public static boolean isCoremod = false;

	protected enum Panes {
		NEWS, OPTIONS, MODPACK, MAPS, TEXTURE
	}

	/**
	 * Launch the application.
	 * 
	 * @param args
	 *            - CLI arguments
	 */
	public static void main(String[] args) {

		ImageIcon splashIcon = new ImageIcon(Toolkit.getDefaultToolkit()
				.getImage(
						LaunchFrame.class
								.getResource("/image/phazecraftLogo.png")));

		splash = new SplashScreen(splashIcon.getImage());
		splash.setVisible(true);
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e2) {
		}

		tracker.setEnabled(true);
		TrackerUtils.sendPageView("net/ftb/gui/LaunchFrame.java",
				"Launcher Start v" + version);

		if (new File(Settings.getSettings().getInstallPath(),
				"FTBLauncherLog.txt").exists()) {
			new File(Settings.getSettings().getInstallPath(),
					"FTBLauncherLog.txt").delete();
		}

		if (new File(Settings.getSettings().getInstallPath(),
				"MinecraftLog.txt").exists()) {
			new File(Settings.getSettings().getInstallPath(),
					"MinecraftLog.txt").delete();
		}

		DownloadUtils thread = new DownloadUtils();
		thread.start();

		Logger.logInfo("FTBLaunch starting up (version " + version + ")");
		Logger.logInfo("Java version: " + System.getProperty("java.version"));
		Logger.logInfo("Java vendor: " + System.getProperty("java.vendor"));
		Logger.logInfo("Java home: " + System.getProperty("java.home"));
		Logger.logInfo("Java specification: "
				+ System.getProperty("java.vm.specification.name")
				+ " version: "
				+ System.getProperty("java.vm.specification.version") + " by "
				+ System.getProperty("java.vm.specification.vendor"));
		Logger.logInfo("Java vm: " + System.getProperty("java.vm.name")
				+ " version: " + System.getProperty("java.vm.version") + " by "
				+ System.getProperty("java.vm.vendor"));
		Logger.logInfo("OS: " + System.getProperty("os.arch") + " "
				+ System.getProperty("os.name") + " "
				+ System.getProperty("os.version"));

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				StyleUtil.loadUiStyles();
				try {
					for (LookAndFeelInfo info : UIManager
							.getInstalledLookAndFeels()) {
						if ("Nimbus".equals(info.getName())) {
							UIManager.setLookAndFeel(info.getClassName());
							break;
						}
					}
				} catch (Exception e) {
					try {
						UIManager.setLookAndFeel(UIManager
								.getCrossPlatformLookAndFeelClassName());
					} catch (Exception e1) {
					}
				}
				I18N.setupLocale();
				I18N.setLocale(Settings.getSettings().getLocale());

				if (noConfig) {
					InstallDirectoryDialog installDialog = new InstallDirectoryDialog();
					installDialog.setVisible(true);
				}

				File installDir = new File(Settings.getSettings()
						.getInstallPath());
				if (!installDir.exists()) {
					installDir.mkdirs();
				}
				File dynamicDir = new File(OSUtils.getDynamicStorageLocation());
				if (!dynamicDir.exists()) {
					dynamicDir.mkdirs();
				}

				userManager = new UserManager(new File(OSUtils
						.getDynamicStorageLocation(), "logindata"));
				con = new LauncherConsole();

				con.setVisible(true);

				File credits = new File(OSUtils.getDynamicStorageLocation(),
						"credits.txt");

				try {
					if (!credits.exists()) {
						FileOutputStream fos = new FileOutputStream(credits);
						OutputStreamWriter osw = new OutputStreamWriter(fos);

						osw.write("Phazecraft "
								+ System.getProperty("line.separator"));
						osw.flush();

						TrackerUtils.sendPageView(
								"net/ftb/gui/LaunchFrame.java",
								"Unique User (Credits)");
					}

					if (!Settings.getSettings().getLoaded()
							&& !Settings.getSettings().getSnooper()) {
						TrackerUtils.sendPageView(
								"net/ftb/gui/LaunchFrame.java",
								"Unique User (Settings)");
						Settings.getSettings().setLoaded(true);
					}

				} catch (FileNotFoundException e1) {
					Logger.logError(e1.getMessage());
				} catch (IOException e1) {
					Logger.logError(e1.getMessage());
				}

				frame = new LaunchFrame(2);
				instance = frame;
				frame.setVisible(true);

				Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
					@Override
					public void uncaughtException(Thread t, Throwable e) {
						Logger.logError(
								"Unhandled exception in " + t.toString(), e);
					}
				});

			}
		});
	}

	/**
	 * Create the frame.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public LaunchFrame(final int tab) {

		Font minecraft = getMinecraftFont(12);
		setFont(new Font("a_FuturaOrto", Font.PLAIN, 12));
		setResizable(false);
		setTitle(mod + ": Mod Installler");

		setIconImage(Toolkit.getDefaultToolkit().getImage(
				this.getClass().getResource("/image/logo_ftb.png")));
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		setBounds(100, 100, 400, 200);

		dropdown_[0] = I18N.getLocaleString("PROFILE_SELECT");
		dropdown_[1] = I18N.getLocaleString("PROFILE_CREATE");

		String[] dropdown = concatenateArrays(dropdown_, UserManager.getNames()
				.toArray(new String[] {}));
		users = new JComboBox(dropdown);
		if (Settings.getSettings().getLastUser() != null) {
			for (int i = 0; i < dropdown.length; i++) {
				if (dropdown[i].equalsIgnoreCase(Settings.getSettings()
						.getLastUser())) {
					users.setSelectedIndex(i);
				}
			}
		}

		Font largerMinecraft;
		largerMinecraft = minecraft.deriveFont((float) 15.5);

		JLabel modLabel = new JLabel();
		modLabel.setText(mod + " mod installer");
		modLabel.setBounds(10, 10, 390, 20);
		modLabel.setFont(largerMinecraft);
		modLabel.setHorizontalAlignment(JLabel.CENTER);

		// Setup login button
		login = new JButton("Install");
		login.setBounds(10, 100, 370, 25);
		login.setFont(minecraft);
		login.addActionListener(this);
		login.addKeyListener(this);
		login.addActionListener(new ActionListener() {
			@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent a) {
				doLogin();
			}
		});

		// Setup login button
		uninstall = new JButton("Uninstall");
		uninstall.setBounds(10, 125, 370, 25);
		uninstall.setFont(minecraft);
		uninstall.addActionListener(this);
		uninstall.addKeyListener(this);
		uninstall.addActionListener(new ActionListener() {
			@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent a) {
				doLogin();
			}
		});

		website = new JButton("Visit Mod Website");
		website.setBounds(10, 75, 370, 25);
		website.setFont(minecraft);
		website.addActionListener(this);
		website.addKeyListener(this);
		website.addActionListener(new ActionListener() {
			@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent a) {
				doLogin();
			}
		});

		// Setup remember checkbox
		inject = new JCheckBox("Inject into origonal Minecraft install");
		inject.setBounds(10, 150, 500, 24);
		inject.setFont(minecraft);
		inject.setOpaque(false);
		inject.setBorderPainted(false);
		inject.setFocusPainted(false);
		inject.setContentAreaFilled(false);
		inject.setBorder(null);
		inject.setForeground(Color.WHITE);
		inject.setHorizontalTextPosition(SwingConstants.RIGHT);
		inject.setIconTextGap(10);
		inject.addKeyListener(this);

		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(null);
		buttonsPanel.add(login);
		buttonsPanel.add(modLabel);
		buttonsPanel.add(uninstall);
		buttonsPanel.add(website);
		// buttonsPanel.add(inject);

		this.setContentPane(buttonsPanel);

		addMouseListener(this);
		addMouseMotionListener(this);

		splash.setVisible(false);
	}

	/**
	 * call this to login
	 */
	@SuppressWarnings("deprecation")
	private void doLogin() {

		RESPONSE = new LoginResponse("1:1:" + "" + ":1:");
		runGameUpdater(RESPONSE);

	}

	/**
	 * checks whether an update is needed, and then starts the update process
	 * off
	 * 
	 * @param response
	 *            - the response from the minecraft servers
	 */
	private void runGameUpdater(final LoginResponse response) {

		initializeMods();

		final String installPath = Settings.getSettings().getInstallPath();

		final ProgressMonitor progMonitor = new ProgressMonitor(this,
				"Downloading minecraft...", "", 0, 100);
		final GameUpdateWorker updater = new GameUpdateWorker(new File(
				installPath, "/bin").getPath()) {
			public void done() {
				progMonitor.close();
				try {
					if (get()) {
						Logger.logInfo("Game update complete");
						FileUtils.killMetaInf();
						JOptionPane
								.showMessageDialog(null,
										"The mod was installed.\nLaunch Minecraft like normal");
					} else {
						ErrorUtils
								.tossError("Error occurred during downloading the game");
					}
				} catch (CancellationException e) {
					ErrorUtils.tossError("Game update canceled.");
				} catch (InterruptedException e) {
					ErrorUtils.tossError("Game update interrupted.");
				} catch (ExecutionException e) {
					ErrorUtils.tossError("Failed to download game.");
				} finally {
					enableObjects();
				}
			}
		};

		updater.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (progMonitor.isCanceled()) {
					updater.cancel(false);
				}
				if (!updater.isDone()) {
					int prog = updater.getProgress();
					if (prog < 0) {
						prog = 0;
					} else if (prog > 100) {
						prog = 100;
					}
					progMonitor.setProgress(prog);
					progMonitor.setNote(updater.getStatus());
				}
			}
		});
		updater.execute();

	}

	/**
	 * launch the game with the mods in the classpath
	 * 
	 * @param workingDir
	 *            - install path
	 * @param username
	 *            - the MC username
	 * @param password
	 *            - the MC password
	 */
	public void launchMinecraft(String d, String s){
		
	}

	/**
	 * @param modPackName
	 *            - The pack to install (should already be downloaded)
	 * @throws IOException
	 */
	protected void installMods(String modPackName) throws IOException {
		String installpath = Settings.getSettings().getInstallPath();
		String temppath = OSUtils.getDynamicStorageLocation();
		Logger.logInfo("dirs mk'd");

	}

	/**
	 * "Saves" the settings from the GUI controls into the settings class.
	 */
	public void saveSettings() {
		Settings.getSettings().setLastUser(
				String.valueOf(users.getSelectedItem()));
		instance.optionsPane.saveSettingsInto(Settings.getSettings());
	}

	/**
	 * @param user
	 *            - user added/edited
	 */
	@SuppressWarnings("unchecked")
	public static void writeUsers(String user) {
		try {
			userManager.write();
		} catch (IOException e) {
		}
		String[] usernames = concatenateArrays(dropdown_, UserManager
				.getNames().toArray(new String[] {}));
		users.removeAllItems();
		for (int i = 0; i < usernames.length; i++) {
			users.addItem(usernames[i]);
			if (usernames[i].equals(user)) {
				users.setSelectedIndex(i);
			}
		}
	}

	/**
	 * updates the tpInstall to the available ones
	 * 
	 * @param locations
	 *            - the available locations to install the tp to
	 */
	@SuppressWarnings("unchecked")
	public static void updateTpInstallLocs(String[] locations) {
		
	}

	/**
	 * updates the mapInstall to the available ones
	 * 
	 * @param locations
	 *            - the available locations to install the map to
	 */
	@SuppressWarnings("unchecked")
	public static void updateMapInstallLocs(String[] locations) {

	}

	/**
	 * @param first
	 *            - First array
	 * @param rest
	 *            - Rest of the arrays
	 * @return - Outputs concatenated arrays
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] concatenateArrays(T[] first, T[]... rest) {
		int totalLength = first.length;
		for (T[] array : rest) {
			totalLength += array.length;
		}
		T[] result = Arrays.copyOf(first, totalLength);
		int offset = first.length;
		for (T[] array : rest) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}



	/**
	 * @return - Outputs selected map install index
	 */
	public static int getSelectedMapInstallIndex() {
		return instance.mapInstallLocation.getSelectedIndex();
	}

	/**
	 * @return - Outputs selected texturepack install index
	 */
	public static int getSelectedTPInstallIndex() {
		return instance.tpInstallLocation.getSelectedIndex();
	}

	/**
	 * @return - Outputs LaunchFrame instance
	 */
	public static LaunchFrame getInstance() {
		return instance;
	}

	/**
	 * Enables all items that are disabled upon launching
	 */
	private void enableObjects() {

	}

	/**
	 * Download and install mods
	 * 
	 * @return boolean - represents whether it was successful in initializing
	 *         mods
	 */
	private boolean initializeMods() {
		ModManager man = new ModManager(new JFrame(), true);
		man.setVisible(true);
		if (man.erroneous) {
			return false;
		}

			man.cleanUp();

		return true;
	}

	/**
	 * disables the buttons that are usually active on the footer
	 */
	public void disableMainButtons() {
		serverbutton.setVisible(false);
		launch.setVisible(false);
		edit.setVisible(false);
		users.setVisible(false);
	}

	/**
	 * disables the footer buttons active when the modpack tab is selected
	 */
	public void disableMapButtons() {
		mapInstall.setVisible(false);
		mapInstallLocation.setVisible(false);
		serverMap.setVisible(false);
	}

	/**
	 * disables the footer buttons active when the texture pack tab is selected
	 */
	public void disableTextureButtons() {
		tpInstall.setVisible(false);
		tpInstallLocation.setVisible(false);
	}

	/**
	 * update the footer to the correct buttons for active tab
	 */
	public void updateFooter() {
	}

	/**
	 * updates the buttons/text to language specific
	 */
	public void updateLocale() {
		if (I18N.currentLocale == Locale.deDE) {
			edit.setBounds(420, 20, 120, 30);
			donate.setBounds(330, 20, 80, 30);
			mapInstall.setBounds(620, 20, 190, 30);
			mapInstallLocation.setBounds(420, 20, 190, 30);
			serverbutton.setBounds(420, 20, 390, 30);
			tpInstallLocation.setBounds(420, 20, 190, 30);
			tpInstall.setBounds(620, 20, 190, 30);
		} else {
			edit.setBounds(480, 20, 60, 30);
			donate.setBounds(390, 20, 80, 30);
			mapInstall.setBounds(650, 20, 160, 30);
			mapInstallLocation.setBounds(480, 20, 160, 30);
			serverbutton.setBounds(480, 20, 330, 30);
			tpInstallLocation.setBounds(480, 20, 160, 30);
			tpInstall.setBounds(650, 20, 160, 30);
		}
		launch.setText(I18N.getLocaleString("LAUNCH_BUTTON"));
		edit.setText(I18N.getLocaleString("EDIT_BUTTON"));
		serverbutton.setText(I18N.getLocaleString("DOWNLOAD_SERVER_PACK"));
		mapInstall.setText(I18N.getLocaleString("INSTALL_MAP"));
		serverMap.setText(I18N.getLocaleString("DOWNLOAD_MAP_SERVER"));
		tpInstall.setText(I18N.getLocaleString("INSTALL_TEXTUREPACK"));
		donate.setText(I18N.getLocaleString("DONATE_BUTTON"));
		dropdown_[0] = I18N.getLocaleString("PROFILE_SELECT");
		dropdown_[1] = I18N.getLocaleString("PROFILE_CREATE");
		writeUsers((String) users.getSelectedItem());
		optionsPane.updateLocale();

	}

	private static ArrayList<String> getXmls() {
		ArrayList<String> s = Settings.getSettings().getPrivatePacks();
		if (s == null) {
			s = new ArrayList<String>();
		}
		for (int i = 0; i < s.size(); i++) {
			if (s.get(i).isEmpty()) {
				s.remove(i);
				i--;
			} else {
				String temp = s.get(i);
				if (!temp.endsWith(".xml")) {
					s.remove(i);
					s.add(i, temp + ".xml");
				}
			}
		}
		s.add(0, "modpacks.xml");
		return s;
	}

	public int getUnreadNews() {
		int i = 0;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(
					new InputStreamReader(
							new URL(
									"http://launcher.feed-the-beast.com/newsupdate.php")
									.openStream()));
			ArrayList<Long> timeStamps = new ArrayList<Long>();
			String s = reader.readLine();
			s = s.trim();
			String[] str = s.split(",");
			for (String aStr : str) {
				if (!timeStamps.contains(Long.parseLong(aStr))) {
					timeStamps.add(Long.parseLong(aStr));
				}
			}
			long l;
			if (Long.parseLong(Settings.getSettings().getNewsDate()) == 0) {
				l = Long.parseLong(Settings.getSettings().getNewsDate());
			} else {
				l = Long.parseLong(Settings.getSettings().getNewsDate()
						.substring(0, 10));
			}
			for (Long timeStamp : timeStamps) {
				long time = timeStamp;
				if (time > l) {
					i++;
				}
			}

		} catch (Exception e) {
			Logger.logError(e.getMessage(), e);
		}

		return i;
	}

	public void doLaunch() {

			doLogin();

	}

	public final Font getMinecraftFont(int size) {
		Font minecraft;
		try {
			// minecraft = Font.createFont(Font.TRUETYPE_FONT,
			// getResourceAsStream("/font/minecraft.ttf")).deriveFont((float)size);
			minecraft = Font.createFont(
					Font.TRUETYPE_FONT,
					new FileInputStream(this.getClass()
							.getResource("/font/minecraft.ttf").getPath()))
					.deriveFont((float) size);
		} catch (Exception e) {
			e.printStackTrace();
			// Fallback
			// minecraft = new Font("Arial", Font.PLAIN, 12);
			// New Fallback!
			String baseDynamic = OSUtils.getDynamicStorageLocation();
			String baseLink = DownloadUtils
					.getStaticCreeperhostLink("minectaft.ttf");
			try {
				org.apache.commons.io.FileUtils.copyURLToFile(
						new URL(baseLink), new File(baseDynamic
								+ "/minecraft.ttf"));
				minecraft = Font.createFont(Font.TRUETYPE_FONT,
						new FileInputStream(baseDynamic + "/minecraft.ttf"))
						.deriveFont((float) size);
			} catch (MalformedURLException e1) {
				// this is just stupid
				minecraft = new Font("Arial", Font.PLAIN, 12);
				e1.printStackTrace();
			} catch (IOException e1) {
				// fallback on the fallback (Server DOwn)
				minecraft = new Font("Arial", Font.PLAIN, 12);
				e1.printStackTrace();
			} catch (FontFormatException e1) {
				// WTF
				minecraft = new Font("Arial", Font.PLAIN, 12);
				e1.printStackTrace();
			}
		}
		return minecraft;
	}

	public static InputStream getResourceAsStream(String path) {
		InputStream stream = null;
		// path = split[split.length - 1];
		if (stream == null) {
			// if (resource.exists()) {
			try {
				stream = new BufferedInputStream(new FileInputStream(path));
			} catch (IOException ignore) {
				Logger.logInfo("NOOOOOOOOOOOOOOOOOOOOOOOO FOOONNNNNTTTTTTTTTTTT!!!!!!!!!!!!!!!!!!!!!!!!!!");
			}
			// }
		}
		return stream;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {

	}

	@SuppressWarnings("deprecation")
	@Override
	public void keyPressed(KeyEvent e) {

	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (e.getKeyChar() == '~') {
			isAuth = !isAuth;
			Logger.logInfo("Auth: " + isAuth);
		}
		if (e.getKeyChar() == '`') {
			isUpdate = !isUpdate;
			Logger.logInfo("Will Update Jar: " + isUpdate);
		}
		if (e.getKeyChar() == 'c') {
			con.setVisible(false);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		setLocation(e.getXOnScreen() - mouseX, e.getYOnScreen() - mouseY);
	}

	@Override
	public void mouseMoved(MouseEvent e) {

	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	@Override
	public void mouseReleased(MouseEvent e) {

	}

}
