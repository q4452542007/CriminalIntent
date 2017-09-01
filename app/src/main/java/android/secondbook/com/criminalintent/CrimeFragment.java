package android.secondbook.com.criminalintent;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TimePicker;


import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.UUID;


/**
 * Created by Administrator on 2017/8/15.
 */

public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "time";
    private static final String DIALOG_PHOTO = "photo";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_CONTACT = 2;
    private static final int REQUEST_PHOTO=3;

    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private Button mTimeButton;
    private CheckBox mSolvedCheckbox;
    private Button mReportButton;
    private Button mSuspectButton;
    private Button mCallButton;

    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private File mPhotoFile;

    private ViewTreeObserver mViewTreeObserver;

    private Callbacks mCallbacks;

    public interface Callbacks {
        void onCrimeUpdated(Crime crime);
    }

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        CrimeFragment fragment = new CrimeFragment();
        args.putSerializable(ARG_CRIME_ID, crimeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (Callbacks)activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 //     mCrime = new Crime();
//      UUID crimeId = (UUID) getActivity().getIntent().getSerializableExtra(ARG_CRIME_ID);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPause() {
        super.onPause();

        CrimeLab.get(getActivity())
                .updateCrime(mCrime);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.delete_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_delete_crime:
                CrimeLab.get(getActivity()).deleteCrime(mCrime);
                updateCrime();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container,  Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = (EditText) v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
                updateCrime();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mDateButton = (Button) v.findViewById(R.id.crime_date);
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });
        mTimeButton = v.findViewById(R.id.crime_time);
        updateDate();
        mTimeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
                dialog.show(fm, DIALOG_TIME);
            }});


        mSolvedCheckbox = (CheckBox) v.findViewById(R.id.crime_solved);
        mSolvedCheckbox.setChecked(mCrime.isSolved());
        mSolvedCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
                updateCrime();
            }
        });

        mReportButton = v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareCompat.IntentBuilder sc = ShareCompat.IntentBuilder.from(getActivity());
                sc.setType("text/plain");
                sc.setText(getCrimeReport());
                sc.setSubject(getString(R.string.crime_report_subject));
                sc.createChooserIntent();
                sc.startChooser();
            }
        });



        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
//        pickContact.addCategory(Intent.CATEGORY_HOME);            //测试过滤器用
        mSuspectButton = v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });

        mCallButton = v.findViewById(R.id.crime_call);
        mCallButton.setText(R.string.call_suspect);
        mCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri number = Uri.parse("tel:" + mCrime.getPhoneNumber());
                Intent i = new Intent(Intent.ACTION_DIAL, number);
                startActivity(i);
            }
        });

        if (mCrime.getSuspect() != null) {
            mSuspectButton.setText(mCrime.getSuspect());
        }

        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact, PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mSuspectButton.setEnabled(false);
        }

        mPhotoButton = v.findViewById(R.id.crime_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto = mPhotoFile != null && captureImage.resolveActivity(packageManager) !=null;
        mPhotoButton.setEnabled(canTakePhoto);

        if (canTakePhoto){
            Uri uri = Uri.fromFile(mPhotoFile);
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }

        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });

        mPhotoView = v.findViewById(R.id.crime_photo);
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                PhotoFragment dialog = PhotoFragment.newInstance(mPhotoFile.getPath());
                dialog.setTargetFragment(CrimeFragment.this,0);
                dialog.show(fm, DIALOG_PHOTO);
            }
        });
        mViewTreeObserver = getActivity().getWindow().getDecorView().getViewTreeObserver();
        mViewTreeObserver.addOnGlobalLayoutListener(mGlobalLayoutListener);

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateCrime();
            updateDate();
        }else if (requestCode == REQUEST_TIME) {
            Date time = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
            mCrime.setDate(time);
            updateDate();
        }else if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();

            String[] queryFields = new String[] {
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
            };
            Cursor c = getActivity().getContentResolver().query(contactUri, queryFields, null, null, null);

            try {
                if (c.getCount() ==0){
                    return;
                }

                c.moveToFirst();
                String suspect = c.getString(0);
                String number = c.getString(1);
                mCrime.setSuspect(suspect);
                mCrime.setPhoneNumber(number);
                updateCrime();
                mSuspectButton.setText(suspect);

            }finally {
                c.close();
            }

        } else if (requestCode == REQUEST_PHOTO) {
            updateCrime();
        }


    }

    private void updateCrime () {
        CrimeLab.get(getActivity()).updateCrime(mCrime);
        mCallbacks.onCrimeUpdated(mCrime);
    }

    private void updateDate() {
        String date = (String) DateFormat.format("EEEE, MMMM dd, yyyy", mCrime.getDate());
        mDateButton.setText(date);
        String time = (String) DateFormat.format("kk:mm", mCrime.getDate());
        mTimeButton.setText(time);
    }

    private String getCrimeReport() {
        String solvedString = null;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, mCrime.getSuspect());
        }

        String report = getString(R.string.crime_report, mCrime.getTitle(), dateString, solvedString, suspect);

        return report;

    }

    private void updatePhotoView(int width, int height) {
        if (mPhotoView == null && !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), width, height);
            mPhotoView.setImageBitmap(bitmap);
        }
    }

    private final ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            updatePhotoView(mPhotoView.getWidth(), mPhotoView.getHeight());
        }
    };


}
