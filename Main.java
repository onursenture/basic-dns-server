package basicdnsserver;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Onur Senture
 */
public class Main
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException
    {
        //default values
        double initialLoadFactor = 0.33; //Sample Load Factor: 0.666
        int recordsPerBucket = 3; // Sample Number of Records per Bucket: 3
        String recordsAndQueries = "sample_input.txt"; //Records and Queries File Path
        String linearHashing = "lh_output.txt"; //Linear hashing File Structure Output File Path
        String resultsOfQueries = "q_results.txt"; //Results of queries Output File path

        ArrayList<String> tempArgs = new ArrayList<String>();
        for (String s : args)
        {
            tempArgs.add(s);
        }
        if (tempArgs.size() != 5)
        {
            System.out.println("missing variable!...");
        } else
        {
            initialLoadFactor = Double.parseDouble(tempArgs.get(0));
            recordsPerBucket = Integer.parseInt(tempArgs.get(1));
            recordsAndQueries = tempArgs.get(2);
            linearHashing = tempArgs.get(3);
            resultsOfQueries = tempArgs.get(4);
            BasicDnsServer s = new BasicDnsServer(initialLoadFactor, recordsPerBucket, recordsAndQueries, linearHashing, resultsOfQueries);
        }
    }
}
