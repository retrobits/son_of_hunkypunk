package org.andglk.hunkypunk;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;

public class ShortcutPreferencesActivity extends ListActivity {
    private ShortcutItemAdapter adapter;

    private DragSortListView.DropListener onDrop =
            new DragSortListView.DropListener() {
                @Override
                public void drop(int from, int to) {
                    ShortcutItem item = adapter.getItem(from);

                    adapter.notifyDataSetChanged();
                    adapter.remove(item);
                    adapter.insert(item, to);
                    moveShortcutPreference(from, to);
                }
            };

    private DragSortListView.RemoveListener onRemove =
            new DragSortListView.RemoveListener() {
                @Override
                public void remove(int which) {
                    adapter.remove(adapter.getItem(which));

                    SharedPreferences sharedShortcuts = getSharedPreferences("shortcuts", MODE_PRIVATE);
                    SharedPreferences sharedShortcutIDs = getSharedPreferences("shortcutIDs", MODE_PRIVATE);
                    SharedPreferences.Editor shortcutEditor = sharedShortcuts.edit();
                    SharedPreferences.Editor shortcutIDEditor = sharedShortcutIDs.edit();

                    String title = sharedShortcutIDs.getString(which + "", "");
                    shortcutIDEditor.remove(which + "");
                    shortcutEditor.remove(title);


                    for (int i = which + 1; i < sharedShortcutIDs.getAll().size() + 1; i++) {
                        shortcutIDEditor.putString((i - 1) + "", sharedShortcutIDs.getString(i + "", ""));
                    }

                    shortcutIDEditor.remove((sharedShortcutIDs.getAll().size() - 1) + "");

                    shortcutEditor.commit();
                    shortcutIDEditor.commit();
                }
            };

    private DragSortListView.DragScrollProfile ssProfile =
            new DragSortListView.DragScrollProfile() {
                @Override
                public float getSpeed(float w, long t) {
                    if (w > 0.8f) {
                        // Traverse all views in a millisecond
                        return ((float) adapter.getCount()) / 0.001f;
                    } else {
                        return 10.0f * w;
                    }
                }
            };


    private ArrayList<ShortcutItem> list;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final SharedPreferences sharedShortcuts = getSharedPreferences("shortcuts", MODE_PRIVATE);
        SharedPreferences sharedShortcutIDs = getSharedPreferences("shortcutIDs", MODE_PRIVATE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shortcut_list);

        mContext = this;
        DragSortListView lv = (DragSortListView) getListView();
        list = new ArrayList<ShortcutItem>();

        for (int i = 0; i < sharedShortcutIDs.getAll().size(); i++) {
            String title = sharedShortcutIDs.getString(i + "", "");
            String command = sharedShortcuts.getString(title, "");

            list.add(new ShortcutItem(title, command));
        }

        adapter = new ShortcutItemAdapter(list);

        this.setListAdapter(adapter);

        lv.setDropListener(onDrop);
        lv.setRemoveListener(onRemove);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {

                final View promptsView = LayoutInflater.from(mContext).inflate(R.layout.shortcut_preferences_prompt, null);
                final ShortcutItem shortcutItem = adapter.getItem(position);

                final String title = shortcutItem.getTitle(); //sharedShortcutIDs.getString(position + "", "");
                final String command = sharedShortcuts.getString(title, "");

                final EditText inputTitle = (EditText) promptsView.findViewById(R.id.editTitleDialogUserInput);
                final EditText inputCommand = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);

                inputTitle.setText(title);
                inputTitle.setSelection(0, inputTitle.getText().length());

                if (command.endsWith("$"))
                    inputCommand.setText(command.substring(0, command.length() - 1));
                else
                    inputCommand.setText(command);

                final CheckBox checkBox = (CheckBox) promptsView.findViewById(R.id.autoenter);
                if (command.endsWith("$"))
                    checkBox.setChecked(true);

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setView(promptsView);
                builder.setTitle("Edit shortcut");

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String nTitle = inputTitle.getText().toString();
                        String nCommand = inputCommand.getText().toString();

                        if (checkBox.isChecked()) {
                            if (!nCommand.endsWith("$"))
                                nCommand += "$";
                            shortcutItem.setCommand(nCommand);
                        } else if (nCommand.endsWith("$")) {
                            nCommand = nCommand.substring(0, nCommand.length() - 1);
                            shortcutItem.setCommand(nCommand);
                        } else
                            shortcutItem.setCommand(nCommand);

                        shortcutItem.setTitle(nTitle);
                        editShortcutSharedPreference(position, title, nTitle, nCommand);
                        adapter.notifyDataSetChanged();
                    }
                });

                builder.create().show();

            }
        });

    }

    public void addShortcutToSharedPreferences(String title, String command) {
        SharedPreferences sharedPreferences = getSharedPreferences("shortcuts", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(title, command);
        editor.commit();
    }

    public void editShortcutSharedPreference(int position, String oldTitle, String newTitle, String command) {
        SharedPreferences sharedShortcuts = getSharedPreferences("shortcuts", MODE_PRIVATE);
        SharedPreferences sharedShortcutIDs = getSharedPreferences("shortcutIDs", MODE_PRIVATE);
        SharedPreferences.Editor shortcutEditor = sharedShortcuts.edit();
        SharedPreferences.Editor shortcutIDEditor = sharedShortcutIDs.edit();

        shortcutEditor.remove(oldTitle);
        shortcutEditor.putString(newTitle, command);

        shortcutIDEditor.putString(position + "", newTitle);

        shortcutEditor.commit();
        shortcutIDEditor.commit();
    }

    private void moveShortcutPreference(int from, int to) {
        SharedPreferences sharedShortcutIDs = getSharedPreferences("shortcutIDs", MODE_PRIVATE);
        SharedPreferences.Editor sharedShortcutIDsEditor = sharedShortcutIDs.edit();

        String title = sharedShortcutIDs.getString(from + "", "");

        if (from < to) {
            for (int i = from + 1; i <= to; i++) {
                sharedShortcutIDsEditor.putString((i - 1) + "", sharedShortcutIDs.getString(i + "", ""));
            }
            sharedShortcutIDsEditor.putString(to + "", title);
        } else if (from > to) {
            for (int i = from - 1; i >= to; i--) {
                sharedShortcutIDsEditor.putString((i + 1) + "", sharedShortcutIDs.getString(i + "", ""));
            }
            sharedShortcutIDsEditor.putString(to + "", title);
        }
        sharedShortcutIDsEditor.commit();
    }

    public class ShortcutItem {
        private String title;
        private String command;

        public ShortcutItem(String title, String command) {
            this.title = title;
            this.command = command;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        public String getTitle() {
            return title;
        }

        public String getCommand() {
            return command;
        }
    }

    public class ShortcutItemAdapter extends ArrayAdapter<ShortcutItem> {

        public ShortcutItemAdapter(ArrayList<ShortcutItem> list) {
            super(ShortcutPreferencesActivity.this, R.layout.shortcut_list_item, R.id.title, list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);

            if (v != convertView && v != null) {
                ViewHolder holder = new ViewHolder();

                TextView titleView = (TextView) v.findViewById(R.id.title);
                TextView summaryView = (TextView) v.findViewById(R.id.command);
                holder.title = titleView;
                holder.command = summaryView;

                v.setTag(holder);
            }

            ViewHolder holder = (ViewHolder) v.getTag();
            String title = getItem(position).getTitle();
            String command = getItem(position).getCommand();

            holder.title.setText(title);
            if (command.endsWith("$"))
                holder.command.setText(command.substring(0, command.length() - 1));
            else
                holder.command.setText(command);

            return v;
        }
    }

    public class ViewHolder {
        public TextView title;
        public TextView command;
    }
}
