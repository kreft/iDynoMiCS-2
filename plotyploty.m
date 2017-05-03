filebase = 'C:\results\2017.05.03_19.36.29_933_testing_scaling\LBDEM\dump_3.0E-8_';

% Load in data
uData = load( strcat(filebase,'u.dat'));
rData = load( strcat(filebase,'rho.dat'));
xData = load( strcat(filebase,'x.dat'));
yData = load( strcat(filebase,'y.dat'));

%%speed
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

%%direction
bm = HeatMap(xData,'colormap',jet);
bx = bm.plot;
colorbar('Peer', bx); 
caxis(bx, [min(min(xData)) max(max(xData))]); 
 
figure
A = size(yData);
for s = 1:2:A(1)
    for t = 1:2:A(2)
        streamline(yData,xData,s,t)
    end
end
 
figure
quiver(yData,xData)