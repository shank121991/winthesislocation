markov_chains_all:

This program creates markov chains (program markov_chains) for all users for the month with highest data and the next month.
It uses already created folder "--/thesislocation/Data/User stay points(month-maxdata, 50m 30min)/*.png and extracts the user and highest month data and creates file "user_highestdata".

It then uses this file to run program "markov_chains" for all the users.

Steps:

1. Run the first part to create dataframe usern_mnth_df
2. Change destination path in variable "base_path"
3. Change the users in for loop. Example: for users 11 to 20
		"for i in range (11, 21):"

Run the program!