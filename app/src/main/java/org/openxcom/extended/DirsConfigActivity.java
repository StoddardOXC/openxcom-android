package org.openxcom.extended;

import java.io.File;
import java.io.IOException;

import org.openxcom.extended.config.Config;
import org.openxcom.extended.config.DataCheckResult;
import org.openxcom.extended.config.DataChecker;
import org.openxcom.extended.config.Xcom1DataChecker;
import org.openxcom.extended.config.Xcom2DataChecker;
import org.openxcom.extended.util.FilesystemHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import ar.com.daidalos.afiledialog.FileChooserDialog;



public class DirsConfigActivity extends Activity {

	private Config config;

	private CheckBox useAppCacheCheck;
	private CheckBox useAppCacheSaveCheck;
	private CheckBox useAppCacheConfCheck;
	
	private EditText dataPathText;
	private EditText savePathText;
	private EditText confPathText;
	
	// We'll need these to be able to disable them at will.
	private Button dataBrowseButton;
	private Button saveBrowseButton;
	private Button confBrowseButton;
    private Button installButton;
	
	private AlertDialog copyWarningDialog;
	private FileChooserDialog dataDialog;
	private FileChooserDialog saveDialog;
	private FileChooserDialog confDialog;
	private FileChooserDialog installDialog;

    private DataChecker checker;
	
	public Context context;

	private TextView xcom1Status;
	private TextView xcom2Status;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (Build.VERSION.SDK_INT >= 14) {
			setTheme(android.R.style.Theme_DeviceDefault_DialogWhenLarge_NoActionBar); // Theme_DeviceDefault_Light_DialogWhenLarge_NoActionBar
		}
		super.onCreate(savedInstanceState);
		context = this;
        config = Config.getInstance();
        if (config == null) {
            config = Config.createInstance(context);
        }
		setContentView(R.layout.activity_dirs_config);

		dataBrowseButton = (Button) findViewById(R.id.dataBrowseButton);
		saveBrowseButton = (Button) findViewById(R.id.saveBrowseButton);
		confBrowseButton = (Button) findViewById(R.id.confBrowseButton);
        installButton = (Button) findViewById(R.id.dirsInstallFromDirButton);

		useAppCacheCheck = (CheckBox) findViewById(R.id.useDataCacheCheck);
		useAppCacheSaveCheck = (CheckBox) findViewById(R.id.useSaveCacheCheck);
		useAppCacheConfCheck = (CheckBox) findViewById(R.id.useConfCacheCheck);

		dataPathText = (EditText) findViewById(R.id.dataPathEdit);

		savePathText = (EditText) findViewById(R.id.savePathEdit);

		confPathText = (EditText) findViewById(R.id.confPathEdit);

        xcom1Status = (TextView) findViewById(R.id.dirsUfo1Status);

        xcom2Status = (TextView) findViewById(R.id.dirsUfo2Status);

		// Prepare dialogs for showing
		setupDialogs();

        updateStatus();

		// Set view elements according to current preferences
		useAppCacheCheck.setChecked(config.getUseDataCache());
		useAppCacheSaveCheck.setChecked(config.getUseSaveCache());
		useAppCacheConfCheck.setChecked(config.getUseConfCache());
		
		dataPathText.setText(config.getDataFolderPath());
		dataPathText.setInputType(0);
		savePathText.setText(config.getSaveFolderPath());
		savePathText.setInputType(0);
		confPathText.setText(config.getConfFolderPath());
		confPathText.setInputType(0);
		
		saveBrowseButton.setEnabled(!config.getUseSaveCache());
		confBrowseButton.setEnabled(!config.getUseConfCache());

		useAppCacheCheck.setChecked(config.getUseDataCache());
		useAppCacheSaveCheck.setChecked(config.getUseSaveCache());
		useAppCacheConfCheck.setChecked(config.getUseConfCache());

		// Set listeners for checkboxes

		useAppCacheCheck.setOnCheckedChangeListener(
				new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						// Alert the user that this option will cause his data to be
						// copied to the new location, resulting in loss of several megabytes.
						if (isChecked) {
							copyWarningDialog.show();
						} else {
                            config.setUseDataCache(false);
							dataPathText.setText(config.getDataFolderPath());
						}
					}
				} );

		useAppCacheSaveCheck.setOnCheckedChangeListener(
				new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						saveBrowseButton.setEnabled(!isChecked);
						if (isChecked) {
                            savePathText.setText(config.getExternalFilesDir().getAbsolutePath());
						} else {
                            savePathText.setText(config.getSaveFolderPath());
						}
						config.setUseSaveCache(isChecked);
					}
				} );

		useAppCacheConfCheck.setOnCheckedChangeListener(
				new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						confBrowseButton.setEnabled(!isChecked);
						if (isChecked) {
							confPathText.setText(config.getExternalFilesDir().getAbsolutePath());
						} else {
							confPathText.setText(config.getConfFolderPath());
						}
						config.setUseConfCache(isChecked);
					}
				} );
				
	}
	
	@Override
	protected void onStop() {
		config.save();
		dismissDialogs();
		super.onStop();
	}
	
	public void dataButtonPress(View view) {
		dataDialog.show();
	}
	
	public void saveButtonPress(View view) {
		saveDialog.loadFolder(config.getSaveFolderPath());
		saveDialog.show();
	}
	
	public void confButtonPress(View view) {
		confDialog.loadFolder(config.getConfFolderPath());
		confDialog.show();
		
	}

    public void installButtonPress(View view) {
        checker = new Xcom1DataChecker();
        installDialog.show();
    }

    public void installUfo2ButtonPress(View view) {
        checker = new Xcom2DataChecker();
        installDialog.show();
    }
	
	// This prepares our dialog windows to be shown.
	private void setupDialogs() {
		AlertDialog.Builder copyDlgBuilder = new AlertDialog.Builder(this);
		copyDlgBuilder.setTitle(R.string.dirs_warning);
		copyDlgBuilder.setMessage(getString(R.string.dirs_warn_copy_to_private_storage));
		copyDlgBuilder.setCancelable(true);
		copyDlgBuilder.setPositiveButton(android.R.string.yes,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
                        config.setUseDataCache(true);
						useAppCacheCheck.setChecked(true);
						dataPathText.setText(config.getExternalFilesDir().getAbsolutePath());
					}
				});
		copyDlgBuilder.setNegativeButton(android.R.string.no,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		copyDlgBuilder.setOnCancelListener(
				new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						config.setUseDataCache(false);
						useAppCacheCheck.setChecked(false);
						dataPathText.setText(config.getDataFolderPath());
					}
				});

		copyWarningDialog = copyDlgBuilder.create();
		
		dataDialog = new FileChooserDialog(this);
		dataDialog.loadFolder(config.getDataFolderPath());
		if (config.getUseDataCache()) {
			// Ignore ZIP files for now - it'll be too confusing.
			// dataDialog.setFilter(".*zip|.*ZIP");
			dataDialog.setShowConfirmation(true, false);
			dataDialog.setFolderMode(true);
		}
		dataDialog.setFolderMode(true);
		dataDialog.setShowOnlySelectable(true);
		dataDialog.addListener(new FileChooserDialog.OnFileSelectedListener() {
			
			@Override
			public void onFileSelected(Dialog source, File folder, String name) {
				// Do nothing, as we don't exactly care for creating new files.
			}
			
			@Override
			public void onFileSelected(Dialog source, File file) {
				source.hide();
				if (config.getUseDataCache()) {
					if (file.isDirectory()) {
						copyData(file, config.getExternalFilesDir());
					} else {
						try {
							FilesystemHelper.zipExtract(file, config.getExternalFilesDir());
						}
						catch (IOException e) {
							e.printStackTrace();
						}
					}
				} else {
                    config.setDataFolderPath(file.getAbsolutePath());
					dataPathText.setText(config.getDataFolderPath());
                    updateStatus();
				}
                invalidateAssets(false);
			}
		});
		
		saveDialog = new FileChooserDialog(this);
		saveDialog.loadFolder(config.getSaveFolderPath());
		saveDialog.setFolderMode(true);
		saveDialog.setCanCreateFiles(true);
		saveDialog.setShowOnlySelectable(true);
		saveDialog.addListener(new FileChooserDialog.OnFileSelectedListener() {
			
			@Override
			public void onFileSelected(Dialog source, File folder, String name) {
				File saveFolder = new File(folder.getAbsolutePath() + "/" + name);
				if (saveFolder.mkdir()) {
					Toast.makeText(source.getContext(), getString(R.string.dirs_folder_create_success) + saveFolder.getName(), Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(source.getContext(), getString(R.string.dirs_folder_create_fail) + saveFolder.getName(), Toast.LENGTH_LONG).show();
				}
			}

			@Override
			public void onFileSelected(Dialog source, File file) {
				source.hide();
                config.setSaveFolderPath(file.getAbsolutePath());
				savePathText.setText(config.getSaveFolderPath());
			}
			
		});
		
		confDialog = new FileChooserDialog(this);
		confDialog.loadFolder(config.getConfFolderPath());
		confDialog.setFolderMode(true);
		confDialog.setCanCreateFiles(true);
		confDialog.setShowOnlySelectable(true);
		confDialog.addListener(new FileChooserDialog.OnFileSelectedListener() {
			
			@Override
			public void onFileSelected(Dialog source, File folder, String name) {
				File confFolder = new File(folder.getAbsolutePath() + "/" + name);
				if (confFolder.mkdir()) {
					Toast.makeText(source.getContext(), getString(R.string.dirs_folder_create_success) + confFolder.getName(), Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(source.getContext(), getString(R.string.dirs_folder_create_fail) + confFolder.getName(), Toast.LENGTH_LONG).show();
				}
			}

			@Override
			public void onFileSelected(Dialog source, File file) {
				source.hide();
                config.setConfFolderPath(file.getAbsolutePath());
				confPathText.setText(config.getConfFolderPath());
			}
		});

        installDialog = new FileChooserDialog(this);
        if (config.hasOldFiles()) {
            installDialog.loadFolder(config.getOldFilesPath());
        } else {
            installDialog.loadFolder();
        }
        installDialog.setFolderMode(true);
        installDialog.setCanCreateFiles(false);
        installDialog.setShowOnlySelectable(true);
        installDialog.addListener(new FileChooserDialog.OnFileSelectedListener() {
            @Override
            public void onFileSelected(final Dialog source, final File file) {
                DataCheckResult dcr = checker.checkWithPath(file.getAbsolutePath());
                if (dcr.isFound()) {
                    AlertDialog ad = new AlertDialog.Builder(source.getContext())
                            .setCancelable(true)
                            .setMessage(getString(R.string.dirs_data_copy_confirmation).replace("$path", file.getAbsolutePath()))
                            .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    source.hide();
                                    copyData(file, new File(config.getDataFolderPath()));
                                }
                            })
                            .setNegativeButton(getString(android.R.string.no), null)
                            .show();
                } else {
                    new AlertDialog.Builder(source.getContext())
                            .setCancelable(false)
                            .setNeutralButton(getString(android.R.string.ok), null)
                            .setTitle(getString(R.string.dirs_warning))
                            .setMessage(R.string.dirs_warn_no_data)
                            .show();
                }
            }

            @Override
            public void onFileSelected(Dialog source, File folder, String name) {
                // Do nothing.
            }
        });
	}
	
	// This cleans up all dialogs.
	private void dismissDialogs() {
		copyWarningDialog.dismiss();
		dataDialog.dismiss();
		saveDialog.dismiss();
		confDialog.dismiss();
        installDialog.dismiss();
	}
	
	// This will start the preloader, which will update the directories.
	public void applyButtonPress(View view) {
		config.save();
		Intent intent = new Intent(this, PreloaderActivity.class);
		intent.putExtra("calledFrom", "DirsConfigActivity");
		Log.i("DirsConfigActivity", "Launching Preloader to patch our files");
		startActivityForResult(intent, 0);
	}
	
	
	// Pass execution to SDLActivity
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
        Log.i("DirsConfigActivity", "onActivityResult: got back from Preloader, passing execution to OpenXcom");
        if (getCallingActivity() == null) {
            Intent intent = new Intent(this, OpenXcom.class);
            startActivity(intent);
        } else {
            setResult(0, new Intent());
        }
		finish();
	}
	
	// Wrap the copy process in an AsyncTask while showing a ProgressDialog.
	protected void copyData(File inDir, File outDir)
	{
		new AsyncTask<File, String, Void>() {
			
			ProgressDialog pd;
			
			@Override
			protected void onPreExecute() {
				pd = new ProgressDialog(context);
				pd.setTitle(getString(R.string.dirs_copying_data));
				pd.setMessage(getString(R.string.dirs_copy_init));
				pd.setCancelable(false);
				pd.setIndeterminate(true);
				pd.show();
				Log.i("OpenXcom", "AsyncTask started");
			}
			
			public void onProgressUpdate(String... message) {
				pd.setMessage(message[0]);
			}
			
			@Override
			protected Void doInBackground(File... arg0) {
				try {
					Log.i("DirsAsyncTask", "Calling copyFolder...");
                    for(String dirName: checker.getDirChecklist()) {
                        publishProgress(getString(R.string.dirs_copying) + dirName + "...");
                        File in = new File(arg0[0].getAbsolutePath() + "/" + dirName);
                        File out = new File(arg0[1].getAbsolutePath() + "/"
                                + checker.getInstallDir() + "/" + dirName.toUpperCase());
						if (in.exists()) {
							FilesystemHelper.copyFolder(in, out, true);
						} else {
							Log.w("OpenXcom", "[DirsAsyncTask] Couldn't find folder '" + dirName + "', your installation might be incomplete");
						}
                    }
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result) {
				if (pd != null) {
					pd.dismiss();
				}
                updateStatus();
				Log.i("DirsConfigActivity", "Finishing asynctask...");
                invalidateAssets(true);
			}
			
		}.execute(inDir, outDir);
	}

    private void updateStatus() {
        xcom1Status.setText(getString(R.string.dirs_status) + getString(R.string.dirs_status_checking));
        xcom2Status.setText(getString(R.string.dirs_status) + getString(R.string.dirs_status_checking));
        new AsyncTask<Void, Void, Void>() {
            DataCheckResult result;
            Spanned resultDisplay;
            Spanned result2Display;

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                xcom1Status.setText(resultDisplay);
                xcom2Status.setText(result2Display);
            }

            @Override
            protected Void doInBackground(Void... params) {
                checker = new Xcom1DataChecker();
                result = checker.checkWithPath(config.getDataFolderPath() + "/UFO");
                if (result.isFound()) {
                    resultDisplay = Html.fromHtml(getString(R.string.dirs_status) + "<font color=\"#00FF00\">Version: " + result.getVersion() + " (" + result.getNotes() + ") </font>");

                } else {
                    resultDisplay = Html.fromHtml(getString(R.string.dirs_status) + "Status: <font color=\"#FF0000\">Not found (" + result.getNotes() + ")</font>");
                }
                checker = new Xcom2DataChecker();
                result = checker.checkWithPath(config.getDataFolderPath() + "/TFTD");
                if (result.isFound()) {
                    result2Display = Html.fromHtml(getString(R.string.dirs_status) + "<font color=\"#00FF00\">Version: " + result.getVersion() + " (" + result.getNotes() + ") </font>");
                } else {
                    result2Display = Html.fromHtml(getString(R.string.dirs_status) + "<font color=\"#FF0000\">Not found (" + result.getNotes() + ")</font>");
                }
                return null;
            }
        }.execute();
    }

    /**
     * Clears stored MD5 values for assets, effectively forcing unpacking again.
     * @param patchOnly Invalidate only patch MD5
     */
	private void invalidateAssets(boolean patchOnly) {
        final String INVALID = "INVALID";
        config.setAssetVersion("9_ufo-patch.zip", INVALID);
        if (!patchOnly) {
            config.setAssetVersion("1_standard.zip", INVALID);
            config.setAssetVersion("2_UFO.zip", INVALID);
            config.setAssetVersion("3_TFTD.zip", INVALID);
            config.setAssetVersion("7_translations", INVALID);
            config.setAssetVersion("z_nomedia", INVALID);
        }
        config.save();
	}

}
