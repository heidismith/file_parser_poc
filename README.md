# file_parser_poc
Introductory file parser given specific file definitions

General:
    This is a proof of concept of the system described in: https://gist.github.com/dmshann0n/6dfba5ebdebe1098d579

    It is written in Java.  I used minimal libraries as I assumed it is a coding exercise where you would rather see 
    my code than my knowledge of Java libraries.

    This application iterates over all files currently in the data directory and stores the data in the corresponding 
    data tables. It expects matching specification files for each type of data file.

    It includes some basic console output with success messages and/or error reporting of data rows not successfully
    stored in the database.

    I intend it as POC rather than production ready, in order to use it as experiment with the customer to 
    prove out some assumptions and further understand use to design the next set of features.
    For a production system, we would at minimum need:
     * A user interface or API or other method for returning errors and invalid data messages, depending on system use.
     * Return error codes rather than strings and localize the messages.
     * Make the DB improvements listed below.
     * Work with the customer to understand the use of the application to verify/understand assumptions below.


Running the application:
    1. Put all desired files in the data & spec folders.
    2. Run FileParser.java


Database:
I chose to use an embedded H2 database to make the POC easy to run from anywhere.  
In a production application, I would not use H2.
I would use a more robust DB: MySQL or Postgres most likely. I would also add:
    * Connection pooling
    * Separate environment variables for separate databases for testing, stage, production environments.
    * More robust upper/lowercase name management for the tables and column names


Assumptions:
    1. Each data entry in the data files is not allowed to be empty.  I assumed this because the data specification files to do not have nullable/not-nullable flags.
    Alternatives:
      a.  All data entries are allowed to be nullable.
      b.  Add a nullable specification to the data file

    2. If one row of data in a data file has invalid data, still process the other rows in the files.
    Alternatives:
      a. If any row of data is invalid in the file, do not process the whole file.
         i. Validate the whole file before entering into the DB.
         ii. Enter into the database while validating, but rollback the transaction if any fails

    3. Post-file processing / rerunning the application.  I explicitly did *not* make an assumption about what should happen to the processed files and what should happen when you rerun the application with the same files in the data folder.
    It leaves an obvious start to a conversation of what to do about re-running the application & how the application will be used.
      a. When are the users expecting to access new data in the database [i.e. regular intervals (cron job), real time (automatically when new files added), on request, etc]
      b. How is invalid data resolved?
      c. How is valid data resolved [i.e. should processed files be moved or deleted?  Or only files after the last run time, run?]

