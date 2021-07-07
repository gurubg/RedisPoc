package com.solum.poc;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class SaaSTest
{

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException
    {
        String properties_file = null;
        String properties_directory = null;
        String tr_solum_rewe_version = "Solum Test SaaS: 1.0";

        properties_file = "test_saas.properties";
        properties_directory = "C:\\TR_TestSaaS\\";

        Properties properties = new Properties();
        BufferedInputStream stream;
        try
        {
            stream = new BufferedInputStream(new FileInputStream(properties_directory + properties_file));
            properties.load(stream);
            stream.close();
        } catch (IOException e)
        {
            System.out.println("\"test_saas.properties\" properties file not found");
            System.out.println("Please check the location of your C:\\TR_TestSaaS\\test_saas.properties");
            System.out.println("\r\n\r\n=========================================================.");
            System.out.println("SoluM Unassign aborted with error.\r\n");
            System.out.println("Goodbye (Sorry, aborted) !");
            System.exit(0);
        }

        // Create Logfile
        Date log_date = new Date();
        SimpleDateFormat log_date_format = new SimpleDateFormat("ddMMMyyyy_HH-mm-ss");
        String log_str = log_date_format.format(log_date);

        Logger logger = Logger.getLogger("Log");
        FileHandler fh;


        String archive_directory = properties.getProperty("ARCHIVE_DIRECTORY");

        try
        {
            // This block configure the logger with handler and formatter
            fh = new FileHandler(properties.getProperty("LOG_DIRECTORY") + "test_saas" + log_str + ".log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

            // Welcome Text
            logger.info("\r\nWelcome to SoluM " + tr_solum_rewe_version
                    + "\r\nThis tool test STI AIMS SaaS.\r\n\r\n");

            // Archive Directory
            File dir = new File(archive_directory + "execution_on_" + log_str);
            dir.mkdir();
        } catch (SecurityException e)
        {
            exit(logger, getStackTrace(e));
        } catch (IOException e)
        {
            exit(logger, getStackTrace(e));
        }

        // Log Directory
        //String log_directory = properties.getProperty("LOG_DIRECTORY");

        // Resultset Delimiter
        String delimiter = properties.getProperty("DELIMITER");

        // In Direcotry
        String articles_dat = properties.getProperty("ARTICLES_DAT");
        logger.info("ARTICLES_DATE: " + articles_dat);

        // In File Json File
        String in_file_json = properties.getProperty("IN_FILE_JSON");
        logger.info("IN FILE JSON: " + in_file_json);

        // Out File Json File
        String out_file_json = properties.getProperty("OUT_FILE_JSON");
        logger.info("OUT FILE JSON: " + out_file_json);

        // Out Directory
        String out_file = properties.getProperty("OUT_FILE");
        logger.info("OUT FILE: " + out_file);
        
        // URL Interface
        String url_interface = properties.getProperty("URL_INTERFACE");
        logger.info("URL INTERFACE: " + url_interface);
        
        // API KEY
        String api_key = properties.getProperty("API_KEY");
        logger.info("API KEY: " + api_key);
        
        // SAAS Token
        String saas_token = properties.getProperty("SAAS_TOKEN");
        logger.info("SAAS_TOKEN: " + saas_token);

        properties_directory = properties.getProperty("PROPERTIES_DIRECTORY");
        logger.info("PROPERTIES_DIRECTORY: " + properties_directory);
 

        // ---------------------------------------------------------------------
        // Read + Write Data
        // ---------------------------------------------------------------------

        PrintWriter json_writer = null;       
        String json_file = "";

        ArrayList<String> labelid_list = new ArrayList<String>();

        // Read .csv
        BufferedReader datei = null;
        try
        {
            String str = null;
            datei = new BufferedReader(new FileReader(articles_dat));

            while ((str = datei.readLine()) != null)
            {
                str = str.trim();
               	labelid_list.add(str);
                logger.info("Line Read: " + str);
            }
            logger.info(" !! .dat file successfully read in. No: " + labelid_list.size());
        } catch (IOException e)
        {
            exit(logger, getStackTrace(e));
        }
        

        try
        {
            String str = null;            
            datei = new BufferedReader(new FileReader(in_file_json));           
            while ((str = datei.readLine()) != null)
            {               
                json_file = json_file + str;
            }
        } catch (IOException e)
        {
            exit(logger, getStackTrace(e));
        }

        json_writer = new PrintWriter(out_file_json, "UTF-8"); 
        for (int k = 0; k < labelid_list.size(); k++)
        {
        	int no = k + 1;
        	logger.info("Request: " + no);
            String[] tmp = labelid_list.get(k).split(delimiter);              
            String labelID = tmp[0];
            String templateName = tmp[1];
            String articleList = tmp[2];
            
            String body = json_file;
            
            body = body.replace("#LabelID#", labelID);
            body = body.replace("#templateName#", templateName);
            body = body.replace("#articleList#", articleList);


            logger.info(body);
            json_writer.println(no);
            json_writer.println(body);


            URL url = null;
            try
            {
            	// Must be generated manual 
            	// https://eu.common.solumesl.com/common/token
            	if(saas_token.equals(""))
            	   saas_token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Ilg1ZVhrNHh5b2pORnVtMWtsMll0djhkbE5QNC1jNTdkTzZRR1RWQndhTmsifQ.eyJpc3MiOiJodHRwczovL3NvbHVtYjJjc2VnLmIyY2xvZ2luLmNvbS8xNWQ1Mjk1ZC05NjdhLTQ2MWUtYjY5My1jMjFhY2U4ZTMwMzkvdjIuMC8iLCJleHAiOjE2MjU2NTAzOTIsIm5iZiI6MTYyNTY0Njc5MiwiYXVkIjoiYmFiNGYzZGItNjdmZC00NTg3LWJkNWUtM2Q1ZjVjMmMxZjVkIiwiaWRwIjoiTG9jYWxBY2NvdW50Iiwib2lkIjoiMjJiYmQyZDgtY2JlNi00NGY5LTg4MGUtZDU5N2VmYjE3MWE0Iiwic3ViIjoiMjJiYmQyZDgtY2JlNi00NGY5LTg4MGUtZDU5N2VmYjE3MWE0IiwibmFtZSI6IkRvdWdsYXMgUERBIiwibmV3VXNlciI6ZmFsc2UsImV4dGVuc2lvbl9BZG1pbkFwcHJvdmVkIjp0cnVlLCJleHRlbnNpb25fQ3VzdG9tZXJDb2RlIjoiRE9VIiwiZXh0ZW5zaW9uX0N1c3RvbWVyTGV2ZWwiOiIxIiwiZW1haWxzIjpbImRvdWdsYXMtcGRhQHNvbHVtLWVzbHN5c3RlbS5kZSJdLCJ0ZnAiOiJCMkNfMV9ST1BDX0F1dGgiLCJhenAiOiJiYWI0ZjNkYi02N2ZkLTQ1ODctYmQ1ZS0zZDVmNWMyYzFmNWQiLCJ2ZXIiOiIxLjAiLCJpYXQiOjE2MjU2NDY3OTJ9.qrh4k5K0eqXg_WqQ70SCmQyp8nXM5fRiKoFkb9vtMjsqu4f7uzWXuFhPybzfYI0vlOBu3IxWKBVH5jlUkgqMNCwoKCfwAXYo2edF0q0cwRqwl5pHBixi2p33VumK0kbR7bpnJAe7KfEpcyL7kTk6r6OrOzHNDo0WzjVrgHe3cWzo8vZfuHKxVtdULURNjbeSesyWM4M_Lp0wJuKgItIoaqthJsR2DZx75Y4nvzOmpHy9ecegnijDreU3tfsTeQoTCR9-g7mjOHsXcs4hULHArhsTH6hOCxD78NvAlBBa9gMgXjNkeVez7WB0eDVsmcgQ4dEEhrfVFX87INNvFSc_tg";
            	
            	if(url_interface.equals(""))
            	  url_interface = "https://eu.common.solumesl.com/common/api/v1/labels/link/multiple/100?company=DOU";
            	
                url = new URL(url_interface);   
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.setRequestProperty("Content-Length", String.valueOf(body.length()));
    		    //connection.setRequestProperty("api-key", api_key);
                connection.setRequestProperty("authorization", "Bearer " + saas_token);

                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(body);
                writer.flush();
                
                logger.info("! Post request succesfully created.");
                

                BufferedReader reader = null;
				try {
					reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					BufferedReader br = null;

					if (connection.getResponseCode() == 200) {
					    br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					    String strCurrentLine;
					        while ((strCurrentLine = br.readLine()) != null) {
					        	logger.info(strCurrentLine);
					        }
					} else {
					    br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
					    String strCurrentLine;
					        while ((strCurrentLine = br.readLine()) != null) {
					        	logger.info(strCurrentLine);
					        }
					}
				}

                for (String response; (response = reader.readLine()) != null;)
                {
                    //System.out.println(response);                    
                    logger.info("! Response succesfully read.");
                    logger.info(response);
                }

                writer.close();
                reader.close();

                TimeUnit.MILLISECONDS.sleep(10);

            } catch (MalformedURLException e)
            {
            	without_exit(logger, getStackTrace(e));
            } 
            catch (IOException e)
            {
            	without_exit(logger, getStackTrace(e));
            } 
            catch (InterruptedException e)
            {
            	without_exit(logger, getStackTrace(e));
            }
        }
        json_writer.close();

        
        
        // Move Files to execute in Archive
        CopyOption[] options = new CopyOption[]
        { StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES };
        
        File directory = new File(out_file);
        if (directory.isDirectory())
        {
            File[] listFiles = directory.listFiles();

            for (int i = 0; i < listFiles.length; i++)
            {
                String filen = listFiles[i].getName();
                Path FROM = Paths.get(out_file + filen);
                Path TO = Paths.get(archive_directory + "execution_on_" + log_str + "//" + filen);
                try
                {
                    Files.copy(FROM, TO, options);
                } catch (IOException e)
                {
                    exit(logger, getStackTrace(e));
                }
            }
        }
        
        Path FROM = Paths.get(articles_dat);
        Path TO = Paths.get(archive_directory + "execution_on_" + log_str + "//TestAssignments.csv");
        copy_file(logger, FROM, TO, options);

        logger.info("\r\n\r\n=========================================================.");
        logger.info("TR Rewe 7Seg successfully executed.\r\n");
        logger.info("Please check the results in: " + out_file);

        logger.info("Goodbye ! Have a nice day ...");

        logger.info("\r\n\r\n=========================================================.");
        logger.info("TR_TestSaaS successfully executed.\r\n");
        logger.info("Please check the results in: " + out_file);

        logger.info("Goodbye ! Have a nice day ...");
    }

    static void copy_file(Logger logger, Path FROM, Path TO, CopyOption[] options)
    {
        try
        {
            logger.info("File copied from " + FROM + " to " + TO);
            Files.copy(FROM, TO, options);
        } catch (IOException e)
        {
            exit(logger, getStackTrace(e));
        }
    }

    static String getStackTrace(final Throwable throwable)
    {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    static void exit(Logger logger, String str)
    {
        logger.warning("\r\n\r\n=========================================================.");
        logger.warning("ERROR: " + str);
        logger.warning("ERROR: TR_TestSaaS aborted with an error.\r\n");

        logger.info("Goodbye (Aborted) !");
        System.exit(0);
    }

    static void without_exit(Logger logger, String str)
    {
    	logger.warning("!!!! ERROR !!!!");
        logger.warning("\r\n\r\n=========================================================.");
        logger.warning("ERROR: " + str);
        logger.warning("ERROR: TR_TestSaaS throws an error without abort.\r\n");

        logger.info("Goodbye (Aborted) !");
    }

}
