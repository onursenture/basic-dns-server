package basicdnsserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 *
 * @author Onur Senture
 *
 *
 */
public final class BasicDnsServer
{

    private static int boundary = 0;
    private static double loadFactor = 0.001;
    private static int bkfr = 1;
    private static int level;
    private static String recQueFile;
    private static String resultsFile;
    private static String outputFile;
    ArrayList<ArrayList<String>> hashTable;
    ArrayList<Integer> insertedIndexes;
    private static BufferedWriter bw = null;
    private static BufferedWriter bw2 = null;
    private static int testGit = 0;

    public BasicDnsServer(double loadFactor, int bkfr, String recQueFile, String outputFile, String resultsFile) throws FileNotFoundException, IOException
    {
        BasicDnsServer.recQueFile = recQueFile;
        BasicDnsServer.resultsFile = resultsFile;
        BasicDnsServer.outputFile = outputFile;
        BasicDnsServer.loadFactor = loadFactor;
        bw = new BufferedWriter(new FileWriter(resultsFile));
        bw2 = new BufferedWriter(new FileWriter(outputFile));
        insertedIndexes = new ArrayList<Integer>();
        hashTable = new ArrayList<ArrayList<String>>();
        for (int i = 0; i < 10000; i++)
        {
            ArrayList<String> t = new ArrayList<String>();
            hashTable.add(t);
        }
        updateDns();
        //addtestCase(300, false);
    }

    public void addtestCase(int x, boolean flag)
    {
        if (flag)
        {
            ArrayList<String> testList = new ArrayList<String>();
            String randomtext = "a";
            for (int i = 0; i < x; i++)
            {
                randomtext = randomtext.concat("a");
                System.out.print(" " + getHashValue(randomtext) + " ");
                testList.add(randomtext);
                addRecord(testList.get(i));
                System.out.println(hashTable.get(getHashValue(randomtext)));
            }
        }
    }

    public static String toString(int i)
    {
        return "" + i;
    }

    /**
     * At each unit of time, some new records are inserted and some old records are deleted
     * because they would not be valid anymore. First delete the records that are expired,
     * then, add new records to the structure.
     */
    public void updateDns() throws FileNotFoundException, IOException
    {
        BufferedReader br = null;
        br = new BufferedReader(new FileReader(recQueFile));
        String r1 = null;

        while ((r1 = br.readLine()) != null)
        {
            StringTokenizer line = new StringTokenizer(r1, "\n");

            //System.out.println("line");
            while (line.hasMoreTokens())
            {
                StringTokenizer rec_que = new StringTokenizer(line.nextToken(), "\t");


                boolean flag = true; //ilk girisim mi? flagi.
                while (rec_que.hasMoreTokens())
                {

                    if (flag)
                    {

                        StringTokenizer recordEntity = new StringTokenizer(rec_que.nextToken());
                        String record = null;
                        int numberOfEntity = 0;
                        while (recordEntity.hasMoreTokens())
                        {
                            if (numberOfEntity == 0)
                            {
                                record = recordEntity.nextToken();
                                numberOfEntity++;
                            } else if (numberOfEntity < 3)
                            {
                                record = record + " ";
                                record = record.concat(recordEntity.nextToken());
                                numberOfEntity++;
                            } else
                            {

                                //System.out.println("# RECORD# : " + record);
                                addRecord(record);
                                record = null;
                                numberOfEntity = 0;
                            }

                        }
                        //System.out.println("# RECORD# : " + record);
                        addRecord(record);
                    } else
                    { //queries
                        StringTokenizer query = new StringTokenizer(rec_que.nextToken());
                        while (query.hasMoreTokens())
                        {
                            //System.out.println("#ANSWER#: " + answer(query.nextToken()));
                            answer(query.nextToken());
                        }
                        writeAnswers("\n");
                    }
                    flag = false; //ilk girisim degil artık
                }
            }
            decrementTime();
        }
        bw.close();
        writeTable();
        //System.out.println("bitti.");
    }

    public void decrementTime()
    {
        for (int i = 0; i < hashTable.size(); i++)
        {
            if (!hashTable.get(i).isEmpty())
            {
                for (int j = 0; j < hashTable.get(i).size(); j++)
                {
                    String cetrefilli;
                    int lastIndex;
                    String expiration;
                    int expirationInt;
                    cetrefilli = hashTable.get(i).get(j);
                    //System.out.println("before decrement:  " + cetrefilli);
                    lastIndex = cetrefilli.length();
                    expiration = cetrefilli.substring(lastIndex - 1, lastIndex);
                    //System.out.println("expiration:" + expiration);
                    expirationInt = Integer.parseInt(expiration);
                    expirationInt--; 
                    if (expirationInt == 0)
                    {
                        //delete the record here.
                        String remove = hashTable.get(i).remove(j);
                        //System.out.println("#REMOVED# : " + remove);
                        break;
                    }
                    expiration = toString(expirationInt);
                    //set the record here
                    cetrefilli = replaceCharAt(cetrefilli, lastIndex, expiration.charAt(0));
                    hashTable.get(i).set(j, cetrefilli);
                    //System.out.println("table'da son hali: " + hashTable.get(i).get(j));
                }
            }
        }
    }

    public static String replaceCharAt(String s, int position, char c)
    {
        StringBuffer buf = new StringBuffer(s);
        buf.setCharAt(position - 1, c);
        return buf.toString();
    }

    /**
     * Inserts a record to the linear hashing structure
     * First finds hash value, then insert record by considering the hash value.
     */
    public int addRecord(String query)
    {
        int index = getHashValue(query);
        ArrayList<String> insertList = hashTable.get(index);
        if (!hashTable.get(index).contains(query))
        {
            insertList.add(query);
            hashTable.set(index, insertList);
        }
        if (isOverload())
        {
            System.out.println("update is in progress!!!");
            update();
        }
        return index;
    }

    public boolean isOverload()
    {
        int loadLimit = hashTable.size() * bkfr;
        if ((double) (totalItems() / (double) loadLimit) > (double) loadFactor) //esittir ekledim yoktu.
        {
            return true;
        } else
        {
            return false;
        }
    }

    public int totalItems()
    {
        int counter = 0;
        for (int i = 0; i < hashTable.size(); i++)
        {
            counter = counter + hashTable.get(i).size();
        }
        return counter;
    }

    public void update()
    {
        int tempBoundary = boundary;
        boundary = boundary + 1;
        ArrayList<String> bucket = new ArrayList<String>();
        ArrayList<String> temp = hashTable.get(tempBoundary);

        if (boundary == (int) (Math.pow(2, level)))
        {
            boundary = 0;
            level = level + 1;
        }

        for (int i = 0; i < temp.size(); i++)
        {
            if (tempBoundary != getHashValue(temp.get(i)))
            {
                temp.remove(i);
                bucket.add(temp.get(i));
            }
            hashTable.set(tempBoundary, temp);
            hashTable.add(bucket);
        }

    }

    public void deleteRecods()
    {
    }

    /**
     * Answers coming queries (find the IP address of a given URL).
     */
    public String answer(String query) throws IOException
    {
        String address = "-1";
        for (int i = 0; i < hashTable.size(); i++)
        {
            if (!hashTable.get(i).isEmpty())
            {
                for (int j = 0; j < hashTable.get(i).size(); j++)
                {
                    if (hashTable.get(i).get(j).contains(query))
                    {
                        String koftiAnarsist = hashTable.get(i).get(j);
                        StringTokenizer tok = new StringTokenizer(koftiAnarsist);
                        int tempCount = 0;
                        while (tok.hasMoreTokens())
                        {
                            if (tempCount == 1)
                            {
                                address = tok.nextToken();
                                address = address.concat(" ");
                                writeAnswers(address);
                                return address;
                            } else
                            {
                                tok.nextToken();
                                tempCount++;
                            }
                        }
                        return address;
                    }
                }
            }
        }
        writeAnswers("-1 ");
        return "-1"; //If there is no record for a query, just print -1
    }

    public static void writeAnswers(String queryResult) throws IOException
    {
        bw.write(queryResult);
    }

    public void writeTable() throws IOException
    {
        //TODO: duzenli sekilde yazdirmaca.
        int counter = 0;
        boolean lineFlag = false;
        for (int i = 0; i < hashTable.size(); i++)
        {
            if (!hashTable.get(i).isEmpty())
            {
                for (int j = 0; j < hashTable.get(i).size(); j++)
                {
                    
                    bw2.write(hashTable.get(i).get(j) + "\n");
                }
            }
        }
        bw2.close();
    }

    /**
     * Give hash value generated from given String
     * @param domainName
     * @return
     */
    public static int getHashValue(String domainName)
    {
        int total = 0;
        for (int i = 0; i < domainName.length(); i++)
        {
            if (Character.isLetter(domainName.charAt(i)))
            {
                total += i * (Character.getNumericValue(domainName.charAt(i)) - Character.getNumericValue('a') + 1);
            }
        }
        return (total % 10000);
    }
}
