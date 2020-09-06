package com.arpitsingh.mydictionary.adapters;

/**
 * Created by ARPIT SINGH
 * 12/10/19
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.arpitsingh.mydictionary.R;
import com.arpitsingh.mydictionary.model.JsonLexicalCategory;
import com.arpitsingh.mydictionary.model.WordDefinition;
import com.arpitsingh.mydictionary.model.WordLexicalEntry;

import java.util.List;

public class WordAdapter extends ArrayAdapter {
    private Context mContext;
    private List<WordLexicalEntry> lexEntry;

    @Override
    public int getCount() {
        return lexEntry.size();
    }

    public WordAdapter(@NonNull Context context, int resource, @NonNull WordDefinition word) {
        super(context, resource);
        lexEntry = word.lexicalEntries;
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View rootView = convertView;

        if (rootView == null) {
            rootView = LayoutInflater.from(mContext).inflate(R.layout.word_entry, parent, false);
        }

        JsonLexicalCategory lexCategory = lexEntry.get(position).lexCategory;
        List<String> definitions = lexEntry.get(position).definitions;
        List<String> examples = lexEntry.get(position).examples;

        TextView categoryTV = rootView.findViewById(R.id.lex_category);
        TextView definitionTV = rootView.findViewById(R.id.lex_definitions);
        TextView exampleTV = rootView.findViewById(R.id.lex_examples);

        if (lexCategory != null)
            categoryTV.setText(lexCategory.text);

        if (definitions != null) {
            StringBuilder def = new StringBuilder("");
            for (int i = 0; i < definitions.size(); i++) {
                def.append((i + 1) + ". " + definitions.get(i) + "\n");
            }
            if (def.length() > 0)
                def.deleteCharAt(def.length() - 1);
            definitionTV.setText(def.toString());
        }

        if (examples != null) {
            StringBuilder exa = new StringBuilder("");
            for (int i = 0; i < examples.size(); i++) {
                exa.append((i + 1) + ". " + examples.get(i) + "\n");
            }
            if (exa.length() > 0)
                exa.deleteCharAt(exa.length() - 1);
            exampleTV.setText(exa.toString());
        }


        return rootView;
    }
}
