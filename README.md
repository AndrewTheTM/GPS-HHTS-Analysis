# GPS Data Processor 

## Background 

In 2009, The Ohio Department of Transportation, with cooperation from the Ohio-Kentucky-Indiana Regional Council of Governments (OKI) and Miami Valley Regional Planning Commission (MVRPC) undertook a GPS-based household travel survey as a research project AND as an updated dataset for OKI to use.

As far as a research project went, the public agency staff learned a lot about performing a GPS-based household travel survey.  However, at the end of it all, OKI was left with a dataset of questionable quality that was largely determined by methods that were not documented to ODOT, OKI, or MVRPC.  This was the case when the project concluded in the Spring of 2011.

As far as usability of the data, OKI attempted to use the data for travel model supply calculations (trip generation, distribution, and mode choice) in 2012-2014, culminating with a peer review in April of 2014.  It was found that the dataset was deficient in many ways and the models estimated from said data were also deficient.  As such, OKI staff is endeavoring to re-work the data using the latest research outlined at the 2014 Innovations in Travel Modeling Conference and NCHRP Report #775 (AKA NCHRP 08-89).

## Programming Work - Current 

The programming work included is initially to process the GPS files that were provided by the subconsultant that performed the GPS analysis.  No data is included in this due to privacy.  It is expected that others have their own raw GPS files to use.

The current status of this is that it will import the GPS files provided to OKI by their consultant team, but with attention given to the fact that this may need to be done by others with different GPS data formats.

This initially started as a project to just write all the GPS data to a DBF file.  However, the DBF file became unwieldly large and that part was removed.  Right now, the process will input GPS files and perform some calculations based on the points.

## Programming Work - Envisioned 

The following are tasks that need to be included:
1. Trip End Detection
2. Mode Change Detection
3. Mode Imputation
4. Purpose Imputation

## Requirements 

This project uses Apache Maven.  

### Installing Maven (Windows 7 Specific) 

Download from maven.apache.org
Copy files somewhere and get path to bin folder, add to system path
Set JAVA_HOME variable to correct Java location
Open a command prompt and run "mvn --version" to test
