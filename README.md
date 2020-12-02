# java-credibility-tool
Provides credibility score functionalities for project @IsThisAnExpert

The tool can insert a credibility score (e.g. h-index) for a twitter user into the database or calculate a score for a twitter user.
The calculation algorithm checks if a twitter user retweets or references users with remarkable scores themselves.
The algorithm will be improved in the future.

There are 2 possibilities to use the tool:

1) GUI: start the tool without command line arguments

2) Command line: The list of commands are:
--calculate-score twitterusername
--insert-hindex twitterusername h-index
