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
A = size(yData);
for s = 1:2:A(1)
    for t = 1:2:A(2)
        streamline(yData,xData,s,t)
    end
end

figure
quiver(yData,xData)

