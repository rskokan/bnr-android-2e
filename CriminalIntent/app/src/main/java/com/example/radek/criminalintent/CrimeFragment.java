package com.example.radek.criminalintent;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.util.Date;
import java.util.UUID;

/**
 * Created by radek on 09/01/16.
 */
public class CrimeFragment extends Fragment {

    private static final String TAG = "CrimeFragment";
    private static final String ARG_CRIME_ID = "crime_id";

    private static final int REQUEST_CODE_DATE = 0;
    private static final int REQUEST_CODE_CONTACT = 1;
    private static final int REQUEST_CODE_PHOTO = 2;

    private Crime crime;
    private File photoFile;

    private EditText titleField;
    private Button dateButton;
    private CheckBox solvedCheckBox;
    private Button suspectButton;
    private ImageButton photoButton;
    private ImageView photoView;


    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        crime = CrimeLab.get(getActivity()).getCrime(crimeId);
        photoFile = CrimeLab.get(getActivity()).getPhotoFile(crime);

        setHasOptionsMenu(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.get(getActivity()).updateCrime(crime);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);
        titleField = (EditText) v.findViewById(R.id.crime_title);
        titleField.setText(crime.getTitle());
        titleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // nada
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                crime.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // nada
            }
        });

        dateButton = (Button) v.findViewById(R.id.crime_date);
        dateButton.setText(crime.getDate().toString());
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment datePickerFragment = DatePickerFragment.newInstance(crime.getDate());
                datePickerFragment.setTargetFragment(CrimeFragment.this, REQUEST_CODE_DATE);
                datePickerFragment.show(getFragmentManager(), DatePickerFragment.TAG);
            }
        });

        solvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
        solvedCheckBox.setChecked(crime.isSolved());
        solvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                crime.setSolved(isChecked);
            }
        });

        Button reportButton = (Button) v.findViewById(R.id.crime_report);
        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = ShareCompat.IntentBuilder.from(getActivity()).createChooserIntent()
                        .setAction(Intent.ACTION_SEND)
                        .setType("text/plain")
                        .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
                        .putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                startActivity(i);
            }
        });

        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        suspectButton = (Button) v.findViewById(R.id.crime_suspect);
        suspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(pickContact, REQUEST_CODE_CONTACT);
            }
        });

        if (crime.getSuspect() != null) {
            suspectButton.setText(crime.getSuspect());
        }

        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact, PackageManager.MATCH_DEFAULT_ONLY) == null) {
            suspectButton.setEnabled(false);
        }

        photoButton = (ImageButton) v.findViewById(R.id.crime_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto = photoFile != null && captureImage.resolveActivity(packageManager) != null;
        photoButton.setEnabled(canTakePhoto);

        if (canTakePhoto) {
            Uri uri = Uri.fromFile(photoFile);
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }

        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(captureImage, REQUEST_CODE_PHOTO);
            }
        });


        photoView = (ImageView) v.findViewById(R.id.crime_photo);
        updatePhotoView();

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_CODE_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            Log.d(TAG, "onActivityResult() date=" + date.toString());
            crime.setDate(date);
            dateButton.setText(date.toString());
        } else if (requestCode == REQUEST_CODE_CONTACT && data != null) {
            Uri contactUri = data.getData();
            String[] queryFields = new String[] {
                    ContactsContract.Contacts.DISPLAY_NAME
            };
            Cursor c = getActivity().getContentResolver()
                    .query(contactUri, queryFields, null, null, null);
            try {
                if (c.getCount() == 0) {
                    return;
                }

                c.moveToFirst();
                String suspect = c.getString(0);
                crime.setSuspect(suspect);
                suspectButton.setText(suspect);
            } finally {
                c.close();
            }
        } else if (requestCode == REQUEST_CODE_PHOTO) {
            updatePhotoView();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.fragment_crime_delete_crime:
                CrimeLab.get(getActivity()).deleteCrime(crime);
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String getCrimeReport() {
        String solvedString = null;
        if (crime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, crime.getDate()).toString();

        String suspect = crime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_subject, suspect);
        }

        String report = getString(R.string.crime_report, crime.getTitle(), dateString, solvedString, suspect);

        return report;
    }

    private void updatePhotoView() {
        if (photoFile == null || !photoFile.exists()) {
            photoView.setImageDrawable(null);
        } else {
            photoView.setImageBitmap(PictureUtils.getScaledBitmap(photoFile.getPath(), getActivity()));
        }
    }
}
