package org.andglk.hunkypunk;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;
import java.util.Collections;


public class ShortcutPreferencesActivity extends AppCompatActivity {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private ShortcutItemAdapter adapter;
    private DragSortListView listView;
    private ArrayList<ShortcutItem> list;
    private Context mContext;
    private Menu mMenu;

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
                public void remove(final int which) {
                    final ShortcutItem item = adapter.getItem(which);
                    adapter.remove(item);

                    SharedPreferences sharedShortcuts = getSharedPreferences("shortcuts", MODE_PRIVATE);
                    SharedPreferences sharedShortcutIDs = getSharedPreferences("shortcutIDs", MODE_PRIVATE);
                    SharedPreferences.Editor shortcutEditor = sharedShortcuts.edit();
                    SharedPreferences.Editor shortcutIDEditor = sharedShortcutIDs.edit();

                    final String title = sharedShortcutIDs.getString(which + "", "");
                    final String command = sharedShortcuts.getString(title, "");
                    shortcutIDEditor.remove(which + "");
                    shortcutEditor.remove(command);


                    for (int i = which + 1; i < sharedShortcutIDs.getAll().size() + 1; i++) {
                        shortcutIDEditor.putString((i - 1) + "", sharedShortcutIDs.getString(i + "", ""));
                    }

                    shortcutIDEditor.remove((sharedShortcutIDs.getAll().size() - 1) + "");

                    shortcutEditor.commit();
                    shortcutIDEditor.commit();

                    Snackbar snackbar = Snackbar.make(listView, item.getTitle() + " removed!", Snackbar.LENGTH_LONG);
                    snackbar.setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            adapter.insert(item, which);
                            addShortcutToSharedPreferences(title, command);
                            moveShortcutPreference(adapter.getCount() - 1, which);
                        }
                    })
                            .show();


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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final SharedPreferences sharedShortcuts = getSharedPreferences("shortcuts", MODE_PRIVATE);
        SharedPreferences sharedShortcutIDs = getSharedPreferences("shortcutIDs", MODE_PRIVATE);
        super.onCreate(savedInstanceState);
        setTheme(R.style.shortcutpreftheme);
        setContentView(R.layout.shortcut_list);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        mContext = this;
        list = new ArrayList<ShortcutItem>();

        for (int i = 0; i < sharedShortcutIDs.getAll().size(); i++) {
            String title = sharedShortcutIDs.getString(i + "", "");
            String command = sharedShortcuts.getString(title, "");

            list.add(new ShortcutItem(title, command));
        }

        adapter = new ShortcutItemAdapter(list);

        listView = (DragSortListView) findViewById(android.R.id.list);
        listView.setAdapter(adapter);

        listView.setDropListener(onDrop);
        listView.setRemoveListener(onRemove);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {

                final View promptsView = LayoutInflater.from(mContext).inflate(R.layout.shortcut_preferences_prompt, null);
                final ShortcutItem shortcutItem = adapter.getItem(position);

                final String title = shortcutItem.getTitle();
                final String command = sharedShortcuts.getString(title, "");

                final EditText inputTitle = (EditText) promptsView.findViewById(R.id.editTitleDialogUserInput);
                final EditText inputCommand = (EditText) promptsView.findViewById(R.id.editComandDialogUserInput);

                if (command.endsWith("$"))
                    inputCommand.setText(command.substring(0, command.length() - 1));
                else
                    inputCommand.setText(command);

                final CheckBox checkBox = (CheckBox) promptsView.findViewById(R.id.autoenter);
                if (command.endsWith("$"))
                    checkBox.setChecked(true);

                final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setView(promptsView);
                builder.setTitle("Edit shortcut");

                builder.setPositiveButton("Ok", null);
                builder.setNegativeButton("Cancel", null);
                final AlertDialog dialog = builder.create();

                inputTitle.setText(title);
                inputTitle.setSelection(0, inputTitle.getText().length());

                final LinearLayout titleMessageLayout = (LinearLayout) promptsView.findViewById(R.id.titlemessage);
                final TextView titleMessage = (TextView) promptsView.findViewById(R.id.titlemessageText);

                final LinearLayout commandMessageLayout = (LinearLayout) promptsView.findViewById(R.id.commandmessage);
                final TextView commandMessage = (TextView) promptsView.findViewById(R.id.commandmessageText);

                inputTitle.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        if (charSequence.length() == 0) {
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            params.setMargins(0, 0, 0, 50);
                            titleMessageLayout.setLayoutParams(params);
                            titleMessage.setText("Title may not be empty!");
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setClickable(false);
                        } else {
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                            titleMessageLayout.setLayoutParams(params);

                            if (inputCommand.getText().length() != 0)
                                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setClickable(true);

                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                    }
                });

                inputCommand.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        if (charSequence.length() == 0) {
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            params.setMargins(0, 0, 0, 50);
                            commandMessageLayout.setLayoutParams(params);
                            commandMessage.setText("Command may not be empty!");
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setClickable(false);

                        } else {
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                            commandMessageLayout.setLayoutParams(params);
                            if (inputTitle.getText().length() != 0)
                                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setClickable(true);

                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String nTitle = inputTitle.getText().toString();
                                String nCommand = inputCommand.getText().toString();
                                inputTitle.setText(nTitle);
                                inputCommand.setText(nCommand);

                                if (!nTitle.equals("") && !nCommand.equals("")) {
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
                                    dialog.dismiss();
                                }
                            }
                        });
                    }
                });
                dialog.show();
            }
        });

    }

    public void addShortcutToSharedPreferences(String title, String command) {
        SharedPreferences sharedShortcuts = getSharedPreferences("shortcuts", MODE_PRIVATE);
        SharedPreferences sharedShortcutIDs = getSharedPreferences("shortcutIDs", MODE_PRIVATE);
        SharedPreferences.Editor shortcutEditor = sharedShortcuts.edit();
        SharedPreferences.Editor shortcutIDEditor = sharedShortcutIDs.edit();
        try {
            shortcutEditor.putString(title, command);
            shortcutIDEditor.putString(sharedShortcutIDs.getAll().size() + "", title);
            shortcutEditor.apply();
            shortcutIDEditor.apply();
        } catch(Exception e){
            Toast.makeText(this, "Adding failed. Storage exception.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public void editShortcutSharedPreference(int position, String oldTitle, String newTitle, String command) {
        SharedPreferences sharedShortcuts = getSharedPreferences("shortcuts", MODE_PRIVATE);
        SharedPreferences sharedShortcutIDs = getSharedPreferences("shortcutIDs", MODE_PRIVATE);
        SharedPreferences.Editor shortcutEditor = sharedShortcuts.edit();
        SharedPreferences.Editor shortcutIDEditor = sharedShortcutIDs.edit();

        shortcutEditor.remove(oldTitle);
        shortcutEditor.putString(newTitle, command);

        shortcutIDEditor.putString(position + "", newTitle);

        shortcutEditor.apply();
        shortcutIDEditor.apply();
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

    private void restoreDefaultShortcutsPreference() {
        SharedPreferences sharedShortcuts = getSharedPreferences("shortcuts", MODE_PRIVATE);
        SharedPreferences sharedShortcutIDs = getSharedPreferences("shortcutIDs", MODE_PRIVATE);
        SharedPreferences.Editor shortcutEditor = sharedShortcuts.edit();
        SharedPreferences.Editor shortcutIDEditor = sharedShortcutIDs.edit();

        shortcutEditor.clear();
        shortcutIDEditor.clear();

        String[] defaults = new String[]{"look", "examine", "take", "inventory", "ask", "drop", "tell", "again", "open", "close", "give", "show"};
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < defaults.length; i++)
            list.add(defaults[i]);
        Collections.sort(list);

        for (int i = 0; i < list.size(); i++) {
            shortcutEditor.putString(list.get(i) + "", list.get(i));
            shortcutIDEditor.putString(i + "", list.get(i));
        }

        shortcutEditor.apply();
        shortcutIDEditor.apply();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = new MenuInflater(getApplication());
        inflater.inflate(R.layout.shortcut_menu, menu);

        //restore set color item
        restoreIcon(menu);

        mMenu = menu; //save to update color later
        return true;
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);

        restoreIcon(menu);
    }

    private void restoreIcon(Menu menu){
        int old = getSharedPreferences("Color",MODE_PRIVATE).getInt("ID", R.id.blue);
        MenuItem oldItem = menu.findItem(old);
        menu.findItem(R.id.change_color).setIcon(oldItem.getIcon());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final SharedPreferences sharedShortcuts = getSharedPreferences("shortcuts", MODE_PRIVATE);
        final SharedPreferences sharedShortcutIDs = getSharedPreferences("shortcutIDs", MODE_PRIVATE);
        switch (item.getItemId()) {
            case R.id.add_button:
                showAddShortcutDialog();
                return true;

            case R.id.restore_button:
                AlertDialog.Builder restoreBuilder = new AlertDialog.Builder(mContext);
                restoreBuilder.setTitle("Restore shortcuts");
                restoreBuilder.setMessage(R.string.deleteShortcuts);
                restoreBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                restoreBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        adapter.clear();
                        restoreDefaultShortcutsPreference();

                        for (int j = 0; j < sharedShortcutIDs.getAll().size(); j++) {
                            String title = sharedShortcutIDs.getString(j + "", "");
                            String command = sharedShortcuts.getString(title, "");

                            adapter.add(new ShortcutItem(title, command));
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
                restoreBuilder.create().show();
                return true;

            case R.id.help_button:
                DialogBuilder.showShortcutHelpDialog(this);
                return true;
            case R.id.change_color:
                restoreIcon(mMenu); //in case nothing is selected
                return true;
            case R.id.blue:
                setColor(item);
                return true;
            case R.id.grey:
                setColor(item);
                return true;
            case R.id.green:
                setColor(item);
                return true;
            case R.id.red:
                setColor(item);
                return true;
            case R.id.yellow:
                setColor(item);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showAddShortcutDialog() {
        final View promptsView = LayoutInflater.from(mContext).inflate(R.layout.shortcut_preferences_prompt, null);

        final EditText inputTitle = (EditText) promptsView.findViewById(R.id.editTitleDialogUserInput);
        final EditText inputCommand = (EditText) promptsView.findViewById(R.id.editComandDialogUserInput);

        final CheckBox checkBox = (CheckBox) promptsView.findViewById(R.id.autoenter);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(promptsView);
        builder.setTitle("Add new shortcut");
        builder.setPositiveButton("OK", null);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        final AlertDialog dialog = builder.create();

        final LinearLayout titleMessageLayout = (LinearLayout) promptsView.findViewById(R.id.titlemessage);
        final TextView titleMessage = (TextView) promptsView.findViewById(R.id.titlemessageText);

        final LinearLayout commandMessageLayout = (LinearLayout) promptsView.findViewById(R.id.commandmessage);
        final TextView commandMessage = (TextView) promptsView.findViewById(R.id.commandmessageText);

        inputTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() == 0) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0, 0, 50);
                    titleMessageLayout.setLayoutParams(params);
                    titleMessage.setText("Title may not be empty!");
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setClickable(false);
                } else {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                    titleMessageLayout.setLayoutParams(params);

                    if (inputCommand.getText().length() != 0)
                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setClickable(true);

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        inputCommand.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() == 0) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0, 0, 50);
                    commandMessageLayout.setLayoutParams(params);
                    commandMessage.setText("Command may not be empty!");
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setClickable(false);

                } else {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                    commandMessageLayout.setLayoutParams(params);
                    if (inputTitle.getText().length() != 0)
                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setClickable(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String nTitle = inputTitle.getText().toString();
                        String nCommand = inputCommand.getText().toString();
                        inputTitle.setText(nTitle);
                        inputCommand.setText(nCommand);
                        ShortcutItem shortcutItem = new ShortcutItem();

                        if (!nTitle.equals("") && !nCommand.equals("")) {
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
                            addShortcutToSharedPreferences(nTitle, nCommand);
                            adapter.add(shortcutItem);
                            adapter.notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    }
                });
            }
        });
        dialog.show();
    }

    public class ShortcutItem {
        private String title;
        private String command;

        public ShortcutItem(String title, String command) {
            this.title = title;
            this.command = command;
        }

        public ShortcutItem() {
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

    private void setColor(MenuItem item){

        MenuItem current = mMenu.findItem(R.id.change_color);
        current.setIcon(item.getIcon());

        //save color icon
        getSharedPreferences("Color",MODE_PRIVATE).edit().putInt("ID", item.getItemId()).commit();

        //to set color on ext draw
        getSharedPreferences("Color",MODE_PRIVATE).edit().putString("newColor", (String) item.getTitleCondensed()).apply();

    }
}
