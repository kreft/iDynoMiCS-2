% Load in data
uData = load('PeriodicTest_u.dat');
rData = load('PeriodicTest_rho.dat');
xData = load('PeriodicTest_x.dat');
yData = load('PeriodicTest_y.dat');

figure
hm = HeatMap(uData,'colormap',jet);
% close anoying premature heatmap (and all other figures)
close all force 
% 'ax' will be a handle to a standard MATLAB axes.
ax = hm.plot; 
% Turn the colorbar on
colorbar('Peer', ax); 
% Adjust the color limits
caxis(ax, [0 max(max(uData))]); 

figure
for s = 0:1:90
    streamline(yData,xData,s,1)
end

figure
quiver(yData,xData)

