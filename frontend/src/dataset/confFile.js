/* jshint esversion: 6 */

const coolors = {
  "pc_color":"#88E7C0",
  "ua_color":"#ff9f1c",
  "u_color":"#ffbf69",
  "oa_color":"#2ec4b6",
  "o_color":"#cbf3f0"
};

const conf = {
  "node": {
    "OA": { "color": coolors["oa_color"], "textColor": "#000000" },
    "O": { "color": coolors["o_color"], "textColor": "#000000" },
    "UA": { "color": coolors["ua_color"], "textColor": "#000000" },
    "U": { "color": coolors["u_color"], "textColor": "#000000" },
    "PC": { "color": coolors["pc_color"], "textColor": "#000000" },
    "PC0": { "color": "white", "textColor": "#000000" }
  },
  "edges": {"assignments": {"edgeColor": "blue", "linestyle": "solid"},
  "associations": {"edgeColor": "green", "linestyle": "dashed"}},
  "highlight": {"color": "yellow"},
  "selected": {"color": "#66FF33"},
  "node_shape" : {"cellHeight": 80, "cellWidth": 150, "font-size": 13.5, "shape": "ellipse"}, //'bottom-round-rectangle' 'rounded-rectangle'
  "edge_shape" : {"font-size": 14, "font-weight": "bold"},
  "x-scaling" : 0.75,
  "y-scaling" : 1.5
};

export { conf }; 
