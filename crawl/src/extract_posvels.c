/*
-Procedure states( Compute the state of a body relative to another )

-Abstract

   This "cookbook" program demonstrates the use of NAIF S- and P-
   Kernel (SPK) files and subroutines to calculate the state
   (position and velocity) of one solar system body relative to
   another solar system body.

-Disclaimer

   THIS SOFTWARE AND ANY RELATED MATERIALS WERE CREATED BY THE
   CALIFORNIA INSTITUTE OF TECHNOLOGY (CALTECH) UNDER A U.S.
   GOVERNMENT CONTRACT WITH THE NATIONAL AERONAUTICS AND SPACE
   ADMINISTRATION (NASA). THE SOFTWARE IS TECHNOLOGY AND SOFTWARE
   PUBLICLY AVAILABLE UNDER U.S. EXPORT LAWS AND IS PROVIDED "AS-IS"
   TO THE RECIPIENT WITHOUT WARRANTY OF ANY KIND, INCLUDING ANY
   WARRANTIES OF PERFORMANCE OR MERCHANTABILITY OR FITNESS FOR A
   PARTICULAR USE OR PURPOSE (AS SET FORTH IN UNITED STATES UCC
   SECTIONS 2312-2313) OR FOR ANY PURPOSE WHATSOEVER, FOR THE
   SOFTWARE AND RELATED MATERIALS, HOWEVER USED.

   IN NO EVENT SHALL CALTECH, ITS JET PROPULSION LABORATORY, OR NASA
   BE LIABLE FOR ANY DAMAGES AND/OR COSTS, INCLUDING, BUT NOT
   LIMITED TO, INCIDENTAL OR CONSEQUENTIAL DAMAGES OF ANY KIND,
   INCLUDING ECONOMIC DAMAGE OR INJURY TO PROPERTY AND LOST PROFITS,
   REGARDLESS OF WHETHER CALTECH, JPL, OR NASA BE ADVISED, HAVE
   REASON TO KNOW, OR, IN FACT, SHALL KNOW OF THE POSSIBILITY.

   RECIPIENT BEARS ALL RISK RELATING TO QUALITY AND PERFORMANCE OF
   THE SOFTWARE AND ANY RELATED MATERIALS, AND AGREES TO INDEMNIFY
   CALTECH AND NASA FOR ALL THIRD-PARTY CLAIMS RESULTING FROM THE
   ACTIONS OF RECIPIENT IN THE USE OF THE SOFTWARE.

-Input

   The program prompts the user for the following input:

      - The name of a NAIF leapseconds kernel file.
      - The name of a NAIF binary SPK ephemeris file.
      - The name for the observing body.
      - The name for the target body.
      - Number of states to calculate.
      - A time interval of interest.

-Output

      - The light time and stellar aberration corrected state of
        the target body relative to the observing body plus
        the magnitude to the position and velocity vectors.

-Particulars

   The user supplies a NAIF leapseconds kernel file, a NAIF binary
   SPK ephemeris file, valid names for both the target and
   observing bodies, and the time to calculate the body's state.

   The program makes use of the following fundamental CSPICE
   interface routines:

      furnsh_c   ---   makes kernel information available to
                       the user's program.

      str2et_c   ---   converts strings representing time to counts
                       of seconds past the J2000 epoch.

      spkezr_c   ---   computes states of one object relative to
                       another at a user specified epoch.

      et2utc_c   ---   converts an ephemeris time J200 to
                       a formatted UTC string.

      prompt_c   ---   interactively prompt a user for a string input

      prsint_c   ---   parse a string representation of an integer
                       to an integer

      vnorm_c    ---   calculate the magnitude (norm) or a 3-vector.

   For the sake of brevity, this program performs few error checks
   on its inputs. Mistakes will cause the program to crash.

-References

   For additional information, see NAIF IDS Required Reading, and the
   headers of the CSPICE subroutines furnsh_c, spkezr_c, et2utc_c
   and str2et_c.

-Restrictions

   None.

-Literature_References

   None.

-Author_and_Institution

   E.D. Wright     (JPL)

-Version

   -CSPICE Version 2.0.0, 11-NOV-2002   (EDW)

      Modified program to reproduce, as precisely as possible,
      output of the FORTRAN version of the states cookbook.
      Programing style and brevity are secondary to this goal.

   -CSPICE Version 1.0.0, 17-OCT-1999   (EDW)

-&
*/


   /* Include needed headers. */

   #include <stdio.h>
   #include "SpiceUsr.h"

int main(int argc, char *argv[])
{

   /*
   Local constants.
   */

   #define     FILE_SIZE 128
   #define     WORD_SIZE 80


   /*
   Local variables.
   */

   SpiceDouble    state[6];
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
   SpiceInt       i;

   SpiceBoolean   cont;


   /* Introduction. */
   puts( " "                                                  );
   puts( "                Welcome to STATES"                  );
   puts( " "                                                  );
   puts( "This program demonstrates the use of NAIF S- and P-");
   puts( "Kernel (SPK) files and subroutines by computing the");
   puts( "state of a target body as seen from an observing"   );
   puts( "body at a number of epochs within a given time"     );
   puts( "interval."                                          );
   puts( " "                                                  );

   prompt_c ( "Enter the name of the metakernel file: ",
                                                     FILE_SIZE, metakn  );
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

   puts( " " );
   puts( "Working ... Please wait" );
   puts( " " );
  

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
   i    = 1;

   /*
   Perform the state look ups for the number of requested 
   intervals. The loop continues so long as the expression:

            i <= maxpts  &&  cont == SPICETRUE

   evaluates to true.
   */
   printf("Results for target \"%s\", observer \"%s\", frame \"%s\"\n", targ, obs, frame);
   printf("(t (UTC), x (km), y (km), z (km), dx (km/s), dy (km/s), dz (km/s))\n");
   do
      {
      /*
      Compute the state of 'targ' from 'obs' at 'et' in the 'frame'
      reference frame and aberration correction 'abcorr'.
      */
      spkezr_c ( targ, et, frame, abcorr, obs, state, &lt );

      /*
      Convert the ET (ephemeris time) into a UTC time string
      for displaying on the screen.
      */
      et2utc_c ( et, format, prec, WORD_SIZE, utc );

      /* 
      Display the results of the state calculation.
      */
      printf("(%s, %23.16e, %23.16e, %23.16e, %23.16e, %23.16e, %23.16e)\n", 
         utc, state[0], state[1], state[2], state[3], state[4], state[5]);

      /*
      Increment the current et by delta and increment the loop
      counter to mark the next cycle.
      */
      et = et + delta;
      i = i + 1;

      }
   while ( i <= maxpts );


   /* Finis */
   return ( 0 );
}