/*
 * Writes a set of latitudinal coordinates to the specified output file.
 * Adapted from states.c in the cspice tookkit cookbook (cspice/src/cook_c/states.pgm).
 *
 * The executable takes a single argument - a file to write the processed location data to. 
 * If the file does not exist, it will be created. If it does exist, it will be overriden. 
 * The columns for the comma-separated location data are:
 * 
 *    time (sol), time (UTC), radius (km), lattidue (deg.), longitude (deg.)
 *
 */

#include <stdlib.h>
#include <stdio.h>
#include "SpiceUsr.h"

int main(int argc, char *argv[])
{
   #define     FILE_SIZE 128
   #define     WORD_SIZE 80

   SpiceDouble    state[3];
   SpiceDouble    radius;
   SpiceDouble    latitude;
   SpiceDouble    longitude;
   SpiceDouble    lt;
   SpiceDouble    et;
   SpiceDouble    etbeg;
   SpiceDouble    etend;
   SpiceDouble    delta;

   SpiceChar      metakn[FILE_SIZE];
   SpiceChar      targ  [WORD_SIZE];
   SpiceChar      obs   [WORD_SIZE];
   SpiceChar      line  [WORD_SIZE];
   SpiceChar      utcbeg[WORD_SIZE];
   SpiceChar      utcend[WORD_SIZE];
   SpiceChar      utc   [WORD_SIZE];
   SpiceChar      frame [WORD_SIZE];
   SpiceChar      abcorr[WORD_SIZE];
   SpiceChar      answer[WORD_SIZE];

   SpiceChar      format[] = "c";

   SpiceInt       maxpts   = 0;
   SpiceInt       prec     = 0;
   SpiceInt       sol;

   SpiceBoolean   cont;

   char *         fname;
   FILE *         fp;

   fname = argv[1];

   errprt_c ( "SET", 1024, "ALL"  );
   erract_c ( "SET", 1024, "REPORT" );

   prompt_c ("Enter the name of the metakernel file: ", FILE_SIZE, metakn);
   puts (" ");

   /*
   Load the binary SPK file containing the ephemeris data
   that we need.
   */
   furnsh_c ( metakn  );


   prompt_c ( "Enter the name of the observing body: ",
                                                      WORD_SIZE, obs );
   puts (" ");
   prompt_c ( "Enter the name of a target body: ",  WORD_SIZE, targ  );

   puts (" ");


   /* Query for the number of state outputs, then loop. */
   do
      {
      prompt_c( "Enter the number of states to be calculated: ",
                                                     WORD_SIZE, line );
      prsint_c ( line, &maxpts );
      puts( " " );

      /*
      Check for a nonsensical input for the number of
      look ups to perform. 
      */
      if ( maxpts <= 0 )
         {
         puts( "The number of states must be greater than 0.");
         puts( " " );
         }
 
      }
   while ( maxpts <= 0 );


   /* Query for the time interval. */
   if ( maxpts == 1 )
      {
      prompt_c ( "Enter the UTC time: ", WORD_SIZE, utcbeg );
      puts(" ");
      }
   else
      {
      prompt_c ( "Enter the beginning UTC time: ", WORD_SIZE, utcbeg );
      puts(" ");

      prompt_c ( "Enter the ending UTC time: ",    WORD_SIZE, utcend );
      puts(" ");
      }

   prompt_c ( "Enter the inertial reference frame (e.g.:J2000): ",
                                                  WORD_SIZE, frame );
   puts( " ");
 

   /*
   Output a banner for the aberration correction prompt.
   */
   printf( "Type of correction                          "   );
   printf( "    Type of state\n"                            );
   printf( "-----------------------------------------------");
   printf( "--------------\n"                               );
   printf( "\'LT+S\'    Light-time and stellar aberration"  );
   printf( "    Apparent state\n"                           );
   printf( "\'LT\'      Light-time only                  "  );
   printf( "    True state\n"                               );
   printf( "\'NONE\'    No correction                    "  );
   printf( "    Geometric state\n");

   puts( " " );
   prompt_c ( "Enter LT+S, LT, or NONE: ", WORD_SIZE, abcorr );

   printf("Working...");  

   /*
   Convert the UTC time strings into DOUBLE PRECISION ETs.
   */
   if ( maxpts == 1 )
      {
      str2et_c ( utcbeg, &etbeg );
      }
   else
      {
      str2et_c ( utcbeg, &etbeg );
      str2et_c ( utcend, &etend );
      }

   /*
   At each time, compute and print the state of the target body
   as seen by the observer.  The output time will be in calendar
   format, rounded to the nearest seconds.

   delta is the increment between consecutive times.

   Make sure that the number of points is >= 1, to avoid a
   division by zero error.
   */

   if ( maxpts > 1 )
      {
      delta  = ( etend - etbeg ) / ( (SpiceDouble) maxpts - 1.);
      }
   else
      {
      delta = 0.0;
      }


   /* Initialize control variable for the spkezr_c loop. */
   et   = etbeg;
   sol    = 0;

   /*
   Open file for writing.
   */
   fp = fopen(fname, "w");
   if (!fp){
      perror("Error opening file!");
      perror(fname);
      exit(1);
   }

   /*
   Perform the state look ups for the number of requested 
   intervals. The loop continues so long as the expression:

            i <= maxpts  &&  cont == SPICETRUE

   evaluates to true.
   */
   fprintf(fp, "target \"%s\", observer \"%s\", frame \"%s\"\n", targ, obs, frame);
   fprintf(fp, "t (sol), t (UTC), radius(km), lattidue(deg.), longitude(deg.)\n");
   

   do
      {
      printf(".");

      /*
      Compute the state of 'targ' from 'obs' at 'et' in the 'frame'
      reference frame and aberration correction 'abcorr'.
      */
      spkpos_c ( targ, et, frame, abcorr, obs, state, &lt );

      /*
      Convert the ET (ephemeris time) into a UTC time string
      for displaying on the screen.
      */
      et2utc_c ( et, format, prec, WORD_SIZE, utc );

      /**
       * Convert Cartesian coordinates to latitudinal coordinates 
       */
      reclat_c( state, &radius, &latitude, &longitude);

      /* 
      Display the results of the state calculation.
      */
      fprintf(fp, "%d, %s, %23.16e, %23.16e, %23.16e\n", sol, utc, radius, latitude * dpr_c(), -1 * longitude * dpr_c());

      /*
      Increment the current et by delta and increment the loop
      counter to mark the next cycle.
      */
      et = et + delta;
      sol = sol + 1;

      }
   while ( sol < maxpts );

   printf("completed!\n");
   printf("Results written to %s\n", fname);
   fclose(fp);

   /* Finis */
   return ( 0 );
}
