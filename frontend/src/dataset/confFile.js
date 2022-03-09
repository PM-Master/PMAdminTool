/* jshint esversion: 6 */

const conf = {
  "node": {
    "OA": { "color": "#ff00ff", "textColor": "#000000" },
    "O": { "color": "#ff00aa", "textColor": "#000000" },
    "UA": { "color": "pink", "textColor": "#000000" },
    "U": { "color": "purple", "textColor": "#000000" },
    "PC": { "color": "green", "textColor": "#000000" },
    "PC0": { "color": "white", "textColor": "#000000" }
  },
  "edges": {"assignments": {"edgeColor": "blue", "linestyle": "solid"},
  "associations": {"edgeColor": "green", "linestyle": "dashed"}},
  "highlight": {"color": "yellow"},
  "selected": {"color": "#66FF33"},
  "node_shape" : {"cellHeight": 80, "cellWidth": 150, "font-size": 14, "shape": "ellipse"}, //'bottom-round-rectangle' 'rounded-rectangle'
  "edge_shape" : {"font-size": 14, "font-weight": "bold"},
  "x-scaling" : 0.75,
  "y-scaling" : 1.5
};

export { conf }; 
