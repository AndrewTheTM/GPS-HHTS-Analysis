# GPS Data Processor 

## Background 

In 2009, The Ohio Department of Transportation, with cooperation from the Ohio-Kentucky-Indiana Regional Council of 
Governments (OKI) and Miami Valley Regional Planning Commission (MVRPC) undertook a GPS-based household travel survey
as a research project AND as an updated dataset for OKI to use.

As far as a research project went, the public agency staff learned a lot about performing a GPS-based household travel
survey.  However, at the end of it all, OKI was left with a dataset of questionable quality that was largely 
determined by methods that were not documented to ODOT, OKI, or MVRPC.  This was the case when the project concluded
in the Spring of 2011.

As far as usability of the data, OKI attempted to use the data for travel model supply calculations (trip generation,
distribution, and mode choice) in 2012-2014, culminating with a peer review in April of 2014.  It was found that the
dataset was deficient in many ways and the models estimated from said data were also deficient.  As such, OKI staff
is endeavoring to re-work the data using the latest research outlined at the 2014 Innovations in Travel Modeling
Conference and NCHRP Report #775 (AKA NCHRP 08-89).

## Programming Work - Current 

The programming work included is initially to process the GPS files that were provided by the subconsultant that 
performed the GPS analysis.  No data is included in this due to privacy.  It is expected that others have their own raw
GPS files to use.  The program imports files, reprojects the data to NAD83 State Plane Ohio South Feet, computes time, 
speed, heading/bearing, and clustering at 100, 250, and 500 feet.  The data is then saved to the hard drive (this 
ideally will prevent memory overruns).  In addition, there are parts of the processes to detect stops and perform some
smoothing on the GPS points (based on [1]).

## Programming Work - Envisioned 

The following are tasks that need to be included:

1. General data cleaning and calculations
2. Trip End Detection
3. Mode Change Detection
4. Mode Imputation
5. Purpose Imputation

## Requirements 

This project uses Apache Maven.  Note that the author is new to Apache Maven and would be happy with any help or best 
practices.

### Installing Maven (Windows 7 Specific) 

Download from maven.apache.org
Copy files somewhere and get path to bin folder, add to system path
Set JAVA_HOME variable to correct Java location
Open a command prompt and run "mvn --version" to test

## License

This is licensed under the Apache 2.0 License.  See the LICENSE.txt file included in the source code.

## References

[1] Schuessler, N., and K. W. Axhausen. "Processing GPS Raw Data Without Additional Information". TRB 2009.

[C] Huxtable, Jerry. JH Labs Projection Library. http://www.jhlabs.com/java/maps/proj/index.html.  Licensed under 
Apache License 2.0.

[C] Hexiong?.  JDBF Java reader and writer library for DBF database files. https://code.google.com/p/jdbf/.  Licensed 
under Apache License 2.0.
