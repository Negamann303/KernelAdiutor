/*
 * Copyright (C) 2015-2016 Willi Ye <williye97@gmail.com>
 *
 * This file is part of Kernel Adiutor.
 *
 * Kernel Adiutor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Kernel Adiutor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Kernel Adiutor.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.grarak.kerneladiutor.fragments.kernel;

import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.grarak.kerneladiutor.fragments.ApplyOnBootFragment;
import com.grarak.kerneladiutor.fragments.BaseControlFragment;
import com.grarak.kerneladiutor.utils.Utils;
import com.grarak.kerneladiutor.utils.ViewUtils;
import com.grarak.kerneladiutor.utils.kernel.cpu.CPUFreq;
import com.grarak.kerneladiutor.utils.root.Control;
import com.grarak.kerneladiutor.utils.root.RootFile;
import com.grarak.kerneladiutor.views.recyclerview.DescriptionView;
import com.grarak.kerneladiutor.views.recyclerview.RecyclerViewItem;

import java.util.List;

/**
 * Created by willi on 04.05.16.
 */
public class GovernorTunableFragment extends BaseControlFragment {

    private String mPath;
    private int mCPU;
    private String mError;

    @Override
    protected boolean showViewPager() {
        return false;
    }

    @Override
    protected boolean retainInstance() {
        return false;
    }

    @Override
    protected RecyclerView.LayoutManager getLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    @Override
    protected void addItems(List<RecyclerViewItem> items) {
    }

    public void setPath(String path, int cpu) {
        mPath = path;
        mCPU = cpu;
        reload();
    }

    public void setError(String error) {
        mError = error;
    }

    private void reload() {
        clearItems();
        if (mPath == null) return;
        RootFile files = new RootFile(mPath);
        for (final String file : files.list()) {
            final String value = Utils.readFile(mPath + "/" + file);
            if (value != null && !value.isEmpty() && !value.contains("\n")) {
                DescriptionView descriptionView = new DescriptionView();
                descriptionView.setTitle(file);
                descriptionView.setSummary(value);
                descriptionView.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
                    @Override
                    public void onClick(RecyclerViewItem item) {
                        List<Integer> freqs = CPUFreq.getFreqs(mCPU);
                        int freq = Utils.strToInt(value);
                        if (freq != 0 && freqs.indexOf(freq) != -1) {
                            String[] values = new String[freqs.size()];
                            for (int i = 0; i < values.length; i++) {
                                values[i] = String.valueOf(freqs.get(i));
                            }
                            showArrayDialog(values, mPath + "/" + file, file);
                        } else {
                            ViewUtils.dialogEditText(value, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }, new ViewUtils.OnDialogEditTextListener() {
                                @Override
                                public void onClick(String text) {
                                    Control.runSetting(Control.write(text, mPath + "/" + file),
                                            ApplyOnBootFragment.CPU, mPath + "/" + file, getActivity());
                                    getHandler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            reload();
                                        }
                                    }, 200);
                                }
                            }, getActivity()).show();
                        }
                    }
                });
                addItem(descriptionView);
            }
        }

        if (itemsSize() < 1 && mError != null) {
            Snackbar.make(getRootView(), mError, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void showArrayDialog(final String[] values, final String path, String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(values, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Control.runSetting(Control.write(values[which], path), ApplyOnBootFragment.CPU, path, getActivity());
                getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        reload();
                    }
                }, 200);
            }
        }).setTitle(name).show();
    }

}
