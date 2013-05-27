package net.phazecraft.tools;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import net.phazecraft.data.Settings;
import net.phazecraft.gui.LaunchFrame;
import net.phazecraft.log.Logger;
import net.phazecraft.util.DownloadUtils;
import net.phazecraft.util.OSUtils;

@SuppressWarnings("serial")
public class ModManager extends JDialog {
	public static boolean update = false, backup = false, erroneous = false,
			upToDate = false;
	private static String curVersion = "";
	private JPanel contentPane;
	// private double downloadedPerc;
	private final JProgressBar progressBar;
	private final JLabel label;
	private final JLabel version = new JLabel();
	private static String sep = File.separator;
	private boolean mod3 = false;
	JLabel file = new JLabel();
	JFrame frame = new JFrame("Configureing..");

	private class ModManagerWorker extends SwingWorker<Boolean, Void> {
		@Override
		protected Boolean doInBackground() throws IOException,
				NoSuchAlgorithmException {
			upToDate = upToDate();
			if (!upToDate) {
				String installPath = OSUtils.getDynamicStorageLocation();

				erroneous = !downloadModPack();
			}
			return true;
		}

		public void downloadUrl(String filename, String urlString)
				throws IOException, NoSuchAlgorithmException {
			BufferedInputStream in = null;
			FileOutputStream fout = null;
			// progressBar.setValue(0);
			try {
				URL url_ = new URL(urlString);
				in = new BufferedInputStream(url_.openStream());
				fout = new FileOutputStream(filename);
				byte data[] = new byte[1024];
				int count, amount = 0, modPackSize = url_.openConnection()
						.getContentLength(), steps = 0;
				// progressBar.setMaximum(10000);
				while ((count = in.read(data, 0, 1024)) != -1) {
					fout.write(data, 0, count);
					// downloadedPerc += (count * 1.0 / modPackSize) * 100;
					amount += count;
					steps++;
					if (steps > 100) {
						steps = 0;
						// progressBar.setValue((int) downloadedPerc * 100);
						label.setText((amount / 1024) + "Kb / "
								+ (modPackSize / 1024) + "Kb");
					}
				}
			} finally {
				in.close();
				fout.flush();
				fout.close();
			}
		}

		protected boolean downloadModPack() {
			Logger.logInfo("Downloading Mod");
			String dynamicLoc = OSUtils.getDynamicStorageLocation();
			String installPath = Settings.getSettings().getInstallPath();


			Logger.logInfo("Generateing Dirs");

			new File(OSUtils.getDynamicStorageLocation(), "mods").mkdir();
			new File(OSUtils.getDynamicStorageLocation(), "coremods").mkdirs();
			new File(OSUtils.getDynamicStorageLocation(), "config").mkdirs();

			Logger.logInfo("Dirs created");

			progressBar.setEnabled(true);

			Logger.logInfo("Installing Mods");
			file.setText("Installing Mods");

			if (!LaunchFrame.isCoremod)
				try {
					org.apache.commons.io.FileUtils.copyURLToFile(
							new URL(DownloadUtils.getCreeperhostLink("/"
									+ LaunchFrame.modVersion + "/mods/"
									+ LaunchFrame.modName)),
							new File(OSUtils.getDynamicStorageLocation(),
									"mods/" + LaunchFrame.modName));
				} catch (NoSuchAlgorithmException | IOException e) {

				}
			else {
				try {
					org.apache.commons.io.FileUtils.copyURLToFile(
							new URL(DownloadUtils.getCreeperhostLink("/"
									+ LaunchFrame.modVersion + "/coremods/"
									+ LaunchFrame.modName)),
							new File(OSUtils.getDynamicStorageLocation(),
									"coremods/" + LaunchFrame.modName));
				} catch (NoSuchAlgorithmException | IOException e) {

				}
			}

			return true;
		}
	}

	/**
	 * Create the frame.
	 */
	public ModManager(JFrame owner, Boolean model) {
		super(owner, model);
		setResizable(false);
		setTitle("Downloading Mod...");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds(100, 100, 313, 145);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		progressBar = new JProgressBar();
		progressBar.setBounds(10, 94, 278, 22);
		contentPane.add(progressBar);

		JLabel lblDownloadingModPack = new JLabel(
				"<html><body><center>Downloading mod pack...<br/>Please Wait</center></body></html>");
		lblDownloadingModPack.setHorizontalAlignment(SwingConstants.CENTER);
		lblDownloadingModPack.setBounds(0, 5, 313, 30);
		file.setHorizontalAlignment(SwingConstants.CENTER);
		file.setBounds(0, 27, 313, 30);
		version.setHorizontalAlignment(SwingConstants.CENTER);
		version.setBounds(0, 39, 313, 60);
		contentPane.add(lblDownloadingModPack);
		contentPane.add(file);
		contentPane.add(version);
		label = new JLabel("");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setBounds(0, 70, 313, 30);
		contentPane.add(label);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent arg0) {
				ModManagerWorker worker = new ModManagerWorker() {
					@Override
					protected void done() {
						frame.setVisible(false);
						frame.dispose();
						setVisible(false);
						super.done();
					}
				};
				worker.execute();
			}
		});
	}

	private boolean upToDate() throws IOException {
			return true;
		
	}

	public static void cleanUp() {
		
	}
}
