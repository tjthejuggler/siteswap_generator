/*
* Siteswap Generator: Android App for generating juggling siteswaps
* Copyright (C) 2017 Tilman Sinning
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package namlit.siteswapgenerator;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import siteswaplib.Filter;
import siteswaplib.SiteswapGenerator;
import siteswaplib.Siteswap;

public class ShowSiteswaps extends AppCompatActivity implements SiteswapGenerationFragment.SiteswapGenerationCallbacks {

    private static final String TAG_SITESWAP_GENERATION_TASK_FRAGMENT = "siteswap_generation_task_fragment";
    private static final String TAG = "tag";
    private SiteswapGenerator mGenerator = null;
    private LinkedList<Siteswap> mSiteswapList = null;
    private SiteswapGenerator.Status mGenerationStatus = SiteswapGenerator.Status.GENERATING;
    private SiteswapGenerationFragment mSiteswapGenerationFragment;
    private ShareActionProvider mShareActionProvider;

    //tj these were made by me
    public static int iterationCount = 0;
    public static LinkedList<Siteswap> unfilteredListOfSiteswaps = new LinkedList<Siteswap>();
    public static LinkedList<Siteswap> filteredListOfSiteswaps = new LinkedList<Siteswap>();


    ListView mSiteswapListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_siteswaps);

        setTitle(String.format(getString(R.string.show_siteswaps__title_generating)));

        Intent intent = getIntent();
        if(intent != null) {
            mGenerator = (SiteswapGenerator) intent.getSerializableExtra(getString(R.string.intent__siteswap_generator));
        }

        mSiteswapListView = (ListView) findViewById(R.id.siteswap_list);

        FragmentManager fm = getFragmentManager();
        mSiteswapGenerationFragment = (SiteswapGenerationFragment) fm.findFragmentByTag(TAG_SITESWAP_GENERATION_TASK_FRAGMENT);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (mSiteswapGenerationFragment == null) {
            mSiteswapGenerationFragment = new SiteswapGenerationFragment();
            fm.beginTransaction().add(mSiteswapGenerationFragment, TAG_SITESWAP_GENERATION_TASK_FRAGMENT).commit();
        }
        else {
            mSiteswapGenerationFragment.getSiteswapGenerator();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setShareIntent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_siteswap_list, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        setShareIntent();

        return true;
    }

    private void setShareIntent() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        String siteswapString = "";
        if (mSiteswapList != null) {
            StringBuilder stringBuilder = new StringBuilder();
            int char_counter = 0;
            for (Siteswap siteswap: mSiteswapList)
            {
                stringBuilder.append(siteswap.toString() + "\n");
                char_counter += siteswap.period_length();
                if (char_counter >= 1000) {
                    stringBuilder.append(getString(
                            R.string.show_siteswaps__share_to_many_siteswaps));
                    break;
                }
            }
            siteswapString = stringBuilder.toString();
        }
        shareIntent.putExtra(Intent.EXTRA_TEXT, siteswapString);
        shareIntent.setType("text/plain");
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    //tj this whole method was made by me
    public ArrayList<String> getComponentsFromSiteswapToLearn(Integer siteswapToLearn) {
        ArrayList<String> siteswapComponents = new ArrayList<>();
        String siteswapToLearnDoubled = Integer.toString(siteswapToLearn) + Integer.toString(siteswapToLearn);
        int siteswapLength = String.valueOf(siteswapToLearn).length();
        List<Integer> digitsThatCanBeInSiteswaps = new ArrayList<>();
        digitsThatCanBeInSiteswaps.add(0);
        digitsThatCanBeInSiteswaps.add(2);
        for (int startingDigitIndex = 0; startingDigitIndex<siteswapLength;startingDigitIndex++){
            Integer startingDigit = Integer.parseInt(Integer.toString(siteswapToLearn).substring(startingDigitIndex, startingDigitIndex+1));
            if (!digitsThatCanBeInSiteswaps.contains(startingDigit)) {
                digitsThatCanBeInSiteswaps.add(startingDigit);
            }

            for (int componentLength = 2; componentLength < siteswapLength;componentLength++){
                String componentToPossiblyAddToList = siteswapToLearnDoubled.substring(startingDigitIndex,startingDigitIndex+componentLength);
                if (!siteswapComponents.contains(componentToPossiblyAddToList)) {
                    siteswapComponents.add(componentToPossiblyAddToList);
                    Log.d(TAG, "componentToPossiblyAddToList: "+componentToPossiblyAddToList);
                }
            }
        }
        return siteswapComponents;
    }

    //tj this whole method was made by me
    public LinkedList<Siteswap> filterBasedOnLearnSiteswap(LinkedList<Siteswap> listThatNeedsFiltered){

        Integer siteswapToLearn = 7531;
        ArrayList<String> siteswapComponents = new ArrayList<>();
        siteswapComponents = getComponentsFromSiteswapToLearn(siteswapToLearn);
        LinkedList<Siteswap> listToReturn = new LinkedList<>();
        listToReturn.addAll(listThatNeedsFiltered); //we fill our list to return up and then..
        if (listToReturn.size() > 0) {//..we cycle through each siteswap in it..
            for (int indexOfPotentialSiteswap = 0; indexOfPotentialSiteswap < listToReturn.size(); indexOfPotentialSiteswap++) {
                boolean containsComponent = false;
                for (int indexOfCurrentComponent = 0; indexOfCurrentComponent < siteswapComponents.size(); indexOfCurrentComponent++){
                    if (listToReturn.get(indexOfPotentialSiteswap).toString().contains(siteswapComponents.get(indexOfCurrentComponent))){
                        containsComponent = true;//..to see if the contain any of our components..
                    }
                }
                if (!containsComponent) {//..if the don't, then we remove them from the list to return..
                    listToReturn.remove(indexOfPotentialSiteswap);
                    indexOfPotentialSiteswap=indexOfPotentialSiteswap-1;//..and remove 1 from our current index..
                }
            }
        }//todo: we need to test this function
        Log.d(TAG, "filterBasedOnLearnSiteswap: HERE AGAIN");
        //mSiteswapList = mGenerator.getSiteswaps();
        //listToReturn.addAll(mSiteswapList);
        return listToReturn;
    }

//this whole function made by me(after the function above)

    public void generateSiteswapsWithoutButton(){

        Integer mPeriodLength = 4;
//        if (iterationCount==0) {
//            mPeriodLength = 3;
//        }
//        if (iterationCount==1) {
//            mPeriodLength = 4;
//        }
//        if (iterationCount==2) {
//            mPeriodLength = 5;
//        }
//        if (iterationCount==3) {
//            mPeriodLength = 6;
//        }

        Integer mMaxThrow = 10;
        Integer mMinThrow = 0;
        Integer mNumberOfObjects = 4;


        if (iterationCount==0) {
            mNumberOfObjects = 3;
        }
        if (iterationCount==1) {
            mNumberOfObjects = 4;
        }
        if (iterationCount==2) {
            mNumberOfObjects = 5;
        }
        if (iterationCount==3) {
            mNumberOfObjects = 6;
        }

        Integer mNumberOfJugglers = 1;

        LinkedList<Filter> mFilterList = new LinkedList<Filter>();

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String serializedFilterList = sharedPref.getString(getString(R.string.main_activity__settings_filter_list), "");

        if (serializedFilterList != "") {
            try {
                byte b[] = Base64.decode(serializedFilterList, Base64.DEFAULT);
                ByteArrayInputStream bi = new ByteArrayInputStream(b);
                ObjectInputStream si = new ObjectInputStream(bi);
                mFilterList = (LinkedList<Filter>) si.readObject();
                si.close();
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.main_activity__deserialization_error_toast),
                        Toast.LENGTH_SHORT).show();
            }
        }

        Integer mMaxResults = 10000;
        Integer mTimeout = 10;
        boolean mIsRandomGenerationMode = false;

        SiteswapGenerator siteswapGenerator = new SiteswapGenerator(mPeriodLength, mMaxThrow,
                mMinThrow, mNumberOfObjects, mNumberOfJugglers, mFilterList);
        siteswapGenerator.setMaxResults(mMaxResults);
        siteswapGenerator.setTimeoutSeconds(mTimeout);
        siteswapGenerator.setRandomGeneration(mIsRandomGenerationMode);

        Intent intent = new Intent(this, ShowSiteswaps.class);
        intent.putExtra(getString(R.string.intent__siteswap_generator), siteswapGenerator);
        startActivity(intent);



    }

//the begining of this function heavily edited by me, after the two functions above



    private void loadSiteswaps() {

        //CURRENT ISSUES/TODOs:
        //  -it is putting too many activities in the stack
        //  -for some reason when I put iterationCount=0; in if (iterationCount > 3) {
        //      i just get an endless loop
        //  -the list i end up with needs sorted somehow
        //  -i need to make a new edittext for it to get the target siteswap from, or maybe a comma seperated list of components
        //  -i need to make the filter function do its thing based on a siteswap, not just a single component

        //testing github

        //WHAT I HAVE NOW IS A HIJACKED SETUP THAT USES THE PARAMS ABOVE INSTEAD OF WHAT IS INPUT,
        //  NEXT THING TO DO IS MAKE IT CYCLE THROUGH HIJACKED PARAMS BASED ON INPUT PARAMS, AND AS IT CYCLES THROUGH
        //  KEEP TRACK OF THE RESULTS, ONCE WE HAVE ALL THE RESULTS WE WANT, WE FILTER OUT WHAT WE DONT WANT BASED ON THE SITESWAP
        //  WE ARE TRYING TO LEARN, AND PUT WHAT REMAINS INTO THE ADAPTER TO BE SHOWN


        //When I get here I should have an iteration number, if the iteration number is as high as it can go, then
        //  I go on with creating the adapter, but if it isn't, then I use that iteration number to run generateSiteswapsWithoutButton() again
        if (iterationCount < 4) {

            //before we do this we want to put the current results into a global linkedlist which
            //will have the results from generateSiteswapsWithoutButton() added to it on the second
            //run through this function. hopefully that will result in a list on the phone that has both params ........
            //trying to commit.....

            generateSiteswapsWithoutButton();
            //mSiteswapList.clear();
            mSiteswapList = mGenerator.getSiteswaps();//tj instead of plugging this into our adapter, we wan to just shovel all the results
            unfilteredListOfSiteswaps.addAll(mSiteswapList);
            //into a static LinkedList and run then run the whole thing again with slightly different params, like 1 less ball
            iterationCount++;
        }






        if (iterationCount > 3) {//tj this was made by me to try and stop the endless loop, BUT
            //I think we should use something else like a public static int that counts cycles, so we can cycle through a
            //certain number of times with different parameters.maybe limit it at up to 3 balls less than the input number and
            //  three period lengths less than the input number since that would be 9 cycles through, maybe we could up period
            //  length to 4 or 5
            //iterationCount=0;
            //mSiteswapList = mGenerator.getSiteswaps();//tj instead of plugging this into our adapter, we wan to just shovel all the results


            filteredListOfSiteswaps = filterBasedOnLearnSiteswap(unfilteredListOfSiteswaps);//tj this method was made by me


            ArrayAdapter adapter = new ArrayAdapter<Siteswap>(
                    ShowSiteswaps.this, android.R.layout.simple_list_item_1, filteredListOfSiteswaps);
            mSiteswapListView.setAdapter(adapter);
            mSiteswapListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Siteswap siteswap = (Siteswap) parent.getItemAtPosition(position);
                    Intent intent = new Intent(getApplicationContext(), DetailedSiteswapActivity.class);
                    intent.putExtra(getString(R.string.intent_detailed_siteswap_view__siteswap), siteswap);
                    startActivity(intent);
                }
            });


//            switch (mGenerationStatus) {
//                case GENERATING:
//                    setTitle(String.format(getString(R.string.show_siteswaps__title_generating)));
//                    break;
//                case ALL_SITESWAPS_FOUND:
//                    setTitle(String.format(getString(R.string.show_siteswaps__title_found_all), mSiteswapList.size()));
//                    break;
//                case MAX_RESULTS_REACHED:
//                    setTitle(String.format(getString(R.string.show_siteswaps__title_limit_reached), mSiteswapList.size()));
//                    break;
//                case TIMEOUT_REACHED:
//                    setTitle(String.format(getString(R.string.show_siteswaps__title_timeout_reached), mSiteswapList.size()));
//                    break;
//                case MEMORY_FULL:
//                    setTitle(String.format(getString(R.string.show_siteswaps__title_memory_full), mSiteswapList.size()));
//                    break;
//                case CANCELLED:
//                    setTitle(String.format(getString(R.string.show_siteswaps__title_cancelled)));
//                    break;
//            }

            //unfilteredListOfSiteswaps.clear();

        }
    }

    public SiteswapGenerator getSiteswapGenerator() {
        return mGenerator;
    }

    public void onGenerationComplete(SiteswapGenerator generator, SiteswapGenerator.Status status) {
        mGenerator = generator;
        mGenerationStatus = status;
        loadSiteswaps();
        setShareIntent();

    }
}
