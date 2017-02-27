uData = load('PeriodicTest_u.dat');
rData = load('PeriodicTest_rho.dat');

HeatMap(uData)

xData = load('PeriodicTest_x.dat');
yData = load('PeriodicTest_y.dat');

quiver(yData,xData)