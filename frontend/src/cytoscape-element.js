/* jshint esversion: 6 */
import { PolymerElement } from '@polymer/polymer/polymer-element.js';
import cytoscape from "cytoscape";
import dagre from "cytoscape-dagre";

cytoscape.use(dagre);

class CytoscapeElement extends PolymerElement {


  static get properties() {
    return {
      cy: {
        type: Object,
        value: null
      },
      dragXCoordinate: {
        type: Number,
        value: 0
      },
      dragYCoordinate: {
        type: Number,
        value: 0
      },
      dragObject: {
        type: String,
        value: null
      },
      cyName: {
        type: String,
        value: null
      },
    };
  }


  static get is() {
    return 'cytoscape-element';
  }

  constructor() {
    super();
  }

  raw_data() {
    return {
      "nodes": [
        {
          "name": "Patient Records",
          "type": "OA",
          "properties": {},
          "id": 0
        },
        {
          "name": "alice",
          "type": "U",
          "properties": {},
          "id": 0
        },
        {
          "name": "josh",
          "type": "O",
          "properties": {},
          "id": 0
        },
        {
          "name": "super_ua2",
          "type": "UA",
          "properties": {
            "namespace": "super"
          },
          "id": 0
        },
        {
          "name": "super_ua1",
          "type": "UA",
          "properties": {
            "namespace": "super"
          },
          "id": 0
        },
        {
          "name": "Doctors",
          "type": "UA",
          "properties": {},
          "id": 0
        },
        {
          "name": "Staff Objects",
          "type": "OA",
          "properties": {},
          "id": 0
        },
        {
          "name": "super",
          "type": "U",
          "properties": {
            "namespace": "super"
          },
          "id": 0
        },
        {
          "name": "RBAC_rep",
          "type": "OA",
          "properties": {
            "pc": "RBAC"
          },
          "id": 0
        },
        {
          "name": "super_pc",
          "type": "PC",
          "properties": {
            "default_oa": "super_pc_default_OA",
            "namespace": "super",
            "default_ua": "super_pc_default_UA",
            "rep": "super_pc_rep"
          },
          "id": 0
        },
        {
          "name": "RBAC",
          "type": "PC",
          "properties": {
            "default_ua": "RBAC_default_UA",
            "default_oa": "RBAC_default_OA",
            "rep": "RBAC_rep"
          },
          "id": 0
        },
        {
          "name": "bob",
          "type": "U",
          "properties": {},
          "id": 0
        },
        {
          "name": "super_oa",
          "type": "OA",
          "properties": {
            "namespace": "super"
          },
          "id": 0
        },
        {
          "name": "timesheets",
          "type": "O",
          "properties": {},
          "id": 0
        },
        {
          "name": "super_pc_default_OA",
          "type": "OA",
          "properties": {
            "namespace": "super_pc"
          },
          "id": 0
        },
        {
          "name": "Nurses",
          "type": "UA",
          "properties": {},
          "id": 0
        },
        {
          "name": "RBAC_default_OA",
          "type": "OA",
          "properties": {
            "namespace": "RBAC"
          },
          "id": 0
        },
        {
          "name": "john",
          "type": "O",
          "properties": {},
          "id": 0
        },
        {
          "name": "super_pc_rep",
          "type": "OA",
          "properties": {
            "pc": "super_pc",
            "namespace": "super"
          },
          "id": 0
        },
        {
          "name": "super_pc_default_UA",
          "type": "UA",
          "properties": {
            "namespace": "super_pc"
          },
          "id": 0
        },
        {
          "name": "RBAC_default_UA",
          "type": "UA",
          "properties": {
            "namespace": "RBAC"
          },
          "id": 0
        }
      ],
      "assignments": [
        [
          "Staff Objects",
          "RBAC_default_OA"
        ],
        [
          "super_ua1",
          "super_pc"
        ],
        [
          "RBAC_default_OA",
          "RBAC"
        ],
        [
          "super_oa",
          "super_pc"
        ],
        [
          "super_pc_default_UA",
          "super_pc"
        ],
        [
          "super",
          "super_ua1"
        ],
        [
          "super",
          "super_ua2"
        ],
        [
          "super_pc_rep",
          "super_oa"
        ],
        [
          "RBAC_rep",
          "super_oa"
        ],
        [
          "john",
          "Patient Records"
        ],
        [
          "alice",
          "Nurses"
        ],
        [
          "josh",
          "Patient Records"
        ],
        [
          "super_pc_default_OA",
          "super_pc"
        ],
        [
          "Patient Records",
          "RBAC_default_OA"
        ],
        [
          "RBAC_default_UA",
          "RBAC"
        ],
        [
          "Nurses",
          "RBAC_default_UA"
        ],
        [
          "super_ua2",
          "super_pc"
        ],
        [
          "timesheets",
          "Staff Objects"
        ],
        [
          "bob",
          "Doctors"
        ],
        [
          "Doctors",
          "RBAC_default_UA"
        ]
      ],
      "associations": [
        {
          "source": "super_ua1",
          "target": "RBAC_default_OA",
          "operations": [
            "*"
          ]
        },
        {
          "source": "super_ua1",
          "target": "super_oa",
          "operations": [
            "*"
          ]
        },
        {
          "source": "super_ua1",
          "target": "RBAC_default_UA",
          "operations": [
            "*"
          ]
        },
        {
          "source": "RBAC_default_UA",
          "target": "Staff Objects",
          "operations": [
            "read"
          ]
        },
        {
          "source": "Doctors",
          "target": "Patient Records",
          "operations": [
            "read",
            "write"
          ]
        },
        {
          "source": "super_ua1",
          "target": "super_pc_default_UA",
          "operations": [
            "*"
          ]
        },
        {
          "source": "super_ua1",
          "target": "super_ua2",
          "operations": [
            "*"
          ]
        },
        {
          "source": "Nurses",
          "target": "Patient Records",
          "operations": [
            "read"
          ]
        },
        {
          "source": "super_ua2",
          "target": "super_ua1",
          "operations": [
            "*"
          ]
        },
        {
          "source": "super_ua1",
          "target": "super_pc_default_OA",
          "operations": [
            "*"
          ]
        }
      ]
    };
  }

  conf() {
    return {
      "node": {
        "OA": { "color": "#ff00ff", "textColor": "#000000" },
        "O": { "color": "#ff00aa", "textColor": "#000000" },
        "UA": { "color": "pink", "textColor": "#000000" },
        "U": { "color": "purple", "textColor": "#000000" },
        "PC": { "color": "green", "textColor": "#000000" },
        "PC0": { "color": "white", "textColor": "#000000" }
      },
      "assignments": { "edgeColor": "#9dbaea", "linestyle": "solid" },
      "associations": { "edgeColor": "#3362FF", "linestyle": "dashed" }
    };
  }

  setNodesInfo(x, conf) {
    return {
      data: { "id": x.name, "label": x.name, "color": conf.node[x.type].color, "textColor": conf.node[x.type].textColor }
    };
  }

  setAssignments(x, conf) {
    return {
      data: {
        "id": x[0].concat(x[1]), "source": x[0], "target": x[1], "label": "",
        'linecolor': conf.assignments.edgeColor, 'linestyle': conf.assignments.linestyle,
        group: "assignments"
      }
    };
  }

  setAssociations(x, conf) {
    return {
      data: {
        "id": x.source.concat(x.target), "source": x.source, "target": x.target, label: x.operations.join(", "),
        'linecolor': conf.associations.edgeColor, 'linestyle': conf.assignments.linestyle,
        dashes: true, group: "associations"
      }
    };
  }

  getElements() {
    var nodes_ = this.raw_data().nodes.map(x => this.setNodesInfo(x, this.conf()));
    var assignments = this.raw_data().assignments.map(x => this.setAssignments(x, this.conf()));
    var associations = this.raw_data().associations.map(x => this.setAssociations(x, this.conf()));
    var edges_ = assignments.concat(associations);
    return {
      nodes: nodes_,
      edges: edges_
    };
  }

  ready() {
    super.ready();
    console.info("cytoscape is ready");
    // cytoscape.use( cxtmenu );
    // cytoscape.use( edgehandles ); 
    var cellHeight = 80;

    var mycy = cytoscape({

      container: document.getElementById(this.cyName),

      minZoom: 1e-5,
      maxZoom: 1e5,
      // wheelSensitivity: 0.5,

      elements: this.getElements(),
      style: [
        {
          selector: "node",
          style: {
            'background-color': 'data(color)', //'#11479e',
            'color': 'data(textColor)',
            "text-opacity": 0.5,
            'border-width': 3,
            'height': 80,
            'width': 150,
            'label': 'data(id)',
            'content': "data(id)",
            'shape': 'round-rectangle',
            'text-valign': 'center',
            'text-halign': 'center',
            'text-wrap': 'wrap',
            'text-max-width': 100
          }
        },

        {
          selector: "edge",
          style: {
            'label': 'data(label)',
            'line-style': 'data(linestyle)', // 'solid', //'dotted', //'dashed',
            // 'line-dash-pattern': [6, 3], 
            // 'line-cap': 'square',
            'source-arrow-shape': 'square',
            'source-arrow-color': 'data(linecolor)',
            'target-arrow-shape': 'triangle',
            'target-arrow-color': 'data(linecolor)',
            'line-color': 'data(linecolor)',
            // 'target-arrow-color': '#9dbaea',
            'curve-style': 'bezier', //'bezier', //'segments'
            "width": "3px",
          }
        }
      ],
      layout: {
        // name: 'dagre',
        name: 'breadthfirst',
        roots: ['super_pc', 'RBAC'],
        transform: function (node, position) { return { x: position.x, y: window.innerHeight - position.y - (Math.random() * cellHeight) }; }

      }
    });
    this.cy = mycy;
  }

  loadGraph(elements) {
    this.cy.json(JSON.parse(elements));
    this.cy.fit();
  }


  loadGraph1() {
    var elements = {
      nodes: [
        { data: { id: "n4" } },
        { data: { id: "n5" } },
        { data: { id: "n6" } },
        { data: { id: "n7" } },
        { data: { id: "n8" } },
        { data: { id: "n9" } },
        { data: { id: "n10" } }
      ],
      edges: [
        { data: { source: "n4", target: "n5", id: "45", label: "45" } },
        { data: { source: "n4", target: "n6", id: "46", label: "46" } },
        { data: { source: "n6", target: "n7", id: "67", label: "67" } },
        { data: { source: "n6", target: "n8", id: "68", label: "68" } },
        { data: { source: "n8", target: "n9", id: "89", label: "89" } },
        { data: { source: "n8", target: "n10", id: "810", label: "810" } }
      ]
    };

    this.cy.json({ elements: elements });
    this.cy.fit();
  }


  loadGraph2() {
    var elements = {
      nodes: [
        { data: { id: "n0" } },
        { data: { id: "n1" } },
        { data: { id: "n2" } },
        { data: { id: "n3" } }
      ],
      edges: [
        { data: { source: "n0", target: "n1", id: "01", label: "01" } },
        { data: { source: "n1", target: "n2", id: "12", label: "12" } },
        { data: { source: "n1", target: "n3", id: "13", label: "13" } }
      ]
    };

    this.cy.json({ elements: elements });
    this.cy.fit();
  }
}

customElements.define(CytoscapeElement.is, CytoscapeElement);