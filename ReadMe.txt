This project is done by METEHAN GELGI(64178)

This is a revised Simplex calculator with 2-phase. 
You can open project directly with "RevisedSimplexCalculator.jar" file.
"indr262BonusProject" file includes codes for this project.

Code is a little bit complex(I simplified it as much as possible). However you can understand code from comment lines. 



Project:
First, when you open project you are going to get small screen for # of Variables and Constraints.By these numbers table will be created. 


Then, you are going to get fields for LP problem. When you fill all fields. Click solve button(Be careful!: Greater and Less than symbols appear to be marked, but they are not selected by default.Please select them.)


In new screen you will get solution for your LP model. 



Errors: 
There is 3 type of errors in this project which are handled for you.  

1) Empty Field Error. If you leave any field empty you are going to get this error.(Greater and Less than symbols should be selected as well)

2) Unbounded LP error. When this error occurs, prints the place where program stop. 

3) General Error. This error occurs when the LP Model is not appropriate. Also I used this error for some algorithmic problems because for some problems this algorithm does not work well.


This Project Tested with: 

Max z = 3x1 + 5x2
s.t.	x1 <=4
	2x2 <= 12
	3x1 + 2x2 <= 18


Max z = 4x1 + 3x2 + 6x3 
s.t.	3x1 + x2  + 3x3 <=30
	2x1 + 2x2 + 3x3 <=40


Min z = 4x1 + x2 
s.t.	3x1 + x2   = 3
	4x1 + 3x2 >= 6
	1x1 + 2x2 <= 4

Max z = -x1 + x2
s.t.	 x1 + x2 >= 1
	3x1 + 2x2 = 6


Max z = 2x1 + 3x2 
s.t.	x1 + 2x2 + x3 = 4
	x1 +  x2      = 3

