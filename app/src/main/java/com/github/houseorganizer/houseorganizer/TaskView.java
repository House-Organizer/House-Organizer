package com.github.houseorganizer.houseorganizer;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import java.util.function.Consumer;

public final class TaskView {
    public TaskView() {}

    public static void setUpTaskView(Task task, EditText titleEditor, EditText descEditor, Button titleButton) {
        titleEditor.setText(task.getTitle());
        descEditor.setText(task.getDescription());

        titleEditor.addTextChangedListener(new TextChangeListener(titleEditor,
                newTitle -> {
                    task.changeTitle(newTitle);
                    titleButton.setText(newTitle);
        }));

        descEditor.addTextChangedListener( new TextChangeListener(descEditor,  task::changeDescription));
    }

    public static void setUpSubTaskView(Task.SubTask subTask, EditText titleEditor) {
        titleEditor.setText(subTask.getTitle());

        titleEditor.addTextChangedListener(new TextChangeListener(titleEditor, subTask::changeTitle));
    }

    // Calls provided consumer once a text change is registered
    private static class TextChangeListener implements TextWatcher {
        private final Consumer<String> textConsumer;
        private final EditText editor;

        public TextChangeListener(EditText editor, Consumer<String> textConsumer) {
            this.editor = editor;
            this.textConsumer = textConsumer;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            textConsumer.accept(editor.getText().toString());
        }
    }
}
