package org.andglk.hunkypunk;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;

public class DirChooser extends DialogFragment {
    private static final String PARENT_DIR = "..";
    private AlertDialog.Builder builder;
    private ListView list;

    private File currentPath;
    private String[] extension = new String[]{".z1", ".z2", ".z3", ".z4", ".z5", ".z6", ".z7", ".z8", ".zblorb", ".zlb", ".t2", ".t3", ".gam"};

    @Override
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        list = new ListView(getActivity());
        builder = new AlertDialog.Builder(getActivity());
        Toast.makeText(getActivity(), "Please go in a folder and press OK", Toast.LENGTH_SHORT).show();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int which, long id) {
                String fileChosen = (String) list.getItemAtPosition(which);
                File chosenFile = getChosenFile(fileChosen);
                if (chosenFile.isDirectory()) {
                    refresh(chosenFile);
                } else {
                    Toast.makeText(getActivity(), "Please Go in a folder and press OK", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                /** sets the just selected directory and push to SharedPreferneces */
                Paths.setIfDirectory(currentPath);
                SharedPreferences sharedPrefs = getActivity().getSharedPreferences("ifPath", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString("ifPath", Paths.ifDirectory().getAbsolutePath());
                editor.commit();

                try {
                    Toast.makeText(getActivity(), "new Directory: " + currentPath.getCanonicalPath(), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getActivity(), "Cancel", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setView(list);
        refresh(Paths.cardDirectory());
        return builder.create();
    }

    /**
     * Convert a relative filename into an actual File object.
     */
    private File getChosenFile(String fileChosen) {
        if (fileChosen.equals(PARENT_DIR)) {
            if (currentPath.equals(Environment.getExternalStorageDirectory()))
                return Environment.getExternalStorageDirectory();
            return currentPath.getParentFile();
        } else {
            return new File(currentPath, fileChosen);
        }
    }

    private void refresh(File path) {
        this.currentPath = path;
        if (path.exists()) {
            File[] dirs = path.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return (file.isDirectory() && file.canRead());
                }
            });

            /** filters all Files, which don't support any extension */
            File[] files = path.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    if (!file.isDirectory()) {
                        if (!file.canRead()) {
                            return false;
                        } else if (extension == null) {
                            return true;
                        } else {
                            for (int i = 0; i < extension.length; i++) {
                                if (file.getName().toLowerCase().endsWith(extension[i]))
                                    return true;
                            }
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            });

            // convert to an array
            int i = 0;
            String[] fileList;
            if (path.getParentFile() == null) {
                fileList = new String[dirs.length + files.length];
            } else {
                fileList = new String[dirs.length + files.length + 1];
                fileList[i++] = PARENT_DIR;
            }
            Arrays.sort(dirs);
            Arrays.sort(files);
            for (File dir : dirs) {
                fileList[i++] = dir.getName();
            }
            for (File file : files) {
                fileList[i++] = file.getName();
            }

            // refresh the user interface
            builder.setTitle(currentPath.getPath());
            list.setAdapter(new ArrayAdapter(getActivity(),
                    android.R.layout.simple_list_item_1, fileList) {
                @Override
                public View getView(int pos, View view, ViewGroup parent) {
                    view = super.getView(pos, view, parent);
                    ((TextView) view).setSingleLine(true);
                    return view;
                }
            });
        }
    }
}
