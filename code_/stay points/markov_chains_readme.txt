markov_chains:

This program:

1. Find staypoints
2. Add times between staypoints based on speed
3. Snap staypoints to the knows staypoints to form states
4. Calculate hourly weights for states
5. Create transition matrices
6. Creates markov chains 

For a particular user and month in an online manner.

inputs:
1. User
2. Month
3. Source file path for Geolife data
4. Destination forlder path

Output:
1. Statypoints file
2. State file
3. Transition matrices for each day and a final transition matrix
4. Markov chain for each hour and final markov chain 


Steps:
1. Change user in variable "user"
2. Change the month in variable "month"
3. Change Geolife source file path in variable "file_src"
4. Change destination files path in variable "base_path"

Run the program!