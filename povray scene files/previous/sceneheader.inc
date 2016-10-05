	global_settings{

radiosity
{
count 100						// try 25 or so

 nearest_count 10			// 10 will be ok for now

  error_bound 0.01			// since 0.5 was too large, we use 0.375,
										//  halfway between 0.25 and 0.5

  recursion_limit 1			// Recursion should be near what you want it to be
										//  If you aren't sure, start with 3 or 4
 }
		ambient_light color rgb < 0.8,  0.5,  0.1 > 
}

	
	
	camera { 
		angle 10.0
		location < -6.0, -200.0,  9.0 >
		look_at <-6.0, 0.0,  9.0 >
		sky   <0,0,1>
	}
	
	background {
		color rgb < 1.0,  1.0,  1.0 >
	}

	
light_source { 
    < -17.5, -23.0,  30.0 >
    color rgb < 0.9, 0.8, 0.8>
    area_light <0, 1, 0>, <0, 0, 1>, 1, 1
    adaptive 5
	jitter
}
	
	union {
	#declare RED = color rgb < 1.0 , 0.0 , 0.0 >;
	#declare BLUE = color rgb < 0.0 , 0.0 , 1.0 >;
	#declare GREEN = color rgb < 0.0 , 1.0 , 0.0 >;
	#declare BLACK = color rgb < 0.0 , 0.0 , 0.0 >;
	#declare WHITE = color rgb < 1.0 , 1.0 , 1.0 >;
