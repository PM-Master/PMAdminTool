/* jshint esversion: 6 */
// import { register } from '@polymer/polymer/lib/utils/telemetry';
import {
  PolymerElement
} from '@polymer/polymer/polymer-element.js';
import cytoscape from "cytoscape";
import $ from "jquery";
import {
  conf as cy_conf
} from "./dataset/confFile";
import {
  elements as elts1
} from "./dataset/Single PC - Small";
import {
  elements as elts2
} from "./dataset/Multiple PCs - Small";
import {
  elements as elts3
} from "./dataset/Multiple PCs - Medium";


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
      graphFromVaadin: {
        type: JSON,
        value: null
      },
      childrenData: {
        type: Object,
        value: null
      },
    };
  }


  static get is() {
    return 'cytoscape-element';
  }


  constructor() {
    super();
    this.childrenData = new Map(); // Holds nodes' children info for restoration
    
  }


  getElements(graph) {
    let nodes_ = graph.nodes.map(x => this.setNodesInfo(x, cy_conf));
    let assignments = graph.assignments.map(x => this.setAssignments(x, cy_conf));
    let associations = graph.associations.map(x => this.setAssociations(x, cy_conf));
    let edges_ = assignments.concat(associations);
    return {
      nodes: nodes_,
      edges: edges_
    };
  }

  getRoots(graph) {
    return graph.nodes.filter(x => (x.type === "PC")).map(x => {
      return x.name;
    });
  }


  expand_collapse_node_all(nodeo) {
    if (this.childrenData.get(nodeo.id()).removed) {
      this.expand_node_all(nodeo);
    } else {
      this.collapse_node_all(nodeo);
    }
  }


  expand_collapse_node_one(nodeo) {
    if (this.childrenData.get(nodeo.id()).removed) {
      this.expand_node_one(nodeo);
    } else {
      this.collapse_node_one(nodeo);
    }
  }


  expand_one_collapse_all(nodeo) {
    if (this.childrenData.get(nodeo.id()).removed) {
      this.expand_node_one(nodeo);
    } else {
      this.collapse_node_all(nodeo);
    }
  }


  setAssignments(x, conf) {
    return {
      data: {
        "id": x[0].replace(" ", "").concat(x[1].replace(" ", "")),
        "source": x[1].replace(" ", ""),
        "sourceshape": 'triangle',
        "target": x[0].replace(" ", ""),
        "targetshape": 'square',
        "label": "",
        "linecolor": conf.edges.assignments.edgeColor,
        "linestyle": conf.edges.assignments.linestyle,
        "group": "assignments"
      }
    };
  }


  setAssociations(x, conf) {
    return {
      data: {
        "id": x.source.replace(" ", "").concat(x.target.replace(" ", "")),
        "source": x.source.replace(" ", ""),
        "sourceshape": 'square',
        "target": x.target.replace(" ", ""),
        "targetshape": 'triangle',
        "label": x.operations.join(", "),
        'linecolor': conf.edges.associations.edgeColor,
        'linestyle': conf.edges.associations.linestyle,
        "group": "associations"
      }
    };
  }


  setNodesInfo(x, conf) {
    return {
      data: {
        "id": x.name.replace(" ", ""),
        "label": x.name,
        "color": conf.node[x.type].color,
        "textColor": conf.node[x.type].textColor,
      }
    };
  }


  ready() {
    super.ready();
    console.info("cytoscape is ready");


    // console.log(this.getElements(elts1));
    // console.log(this.getElements(elts2));
    // console.log(this.getElements(elts3));

    this.dataset_= JSON.parse(this.graphFromVaadin);
    // this.dataset_ = elts2;
    // this.dataset_ = elts2;
    // this.dataset_ = elts3;

    console.log(this.dataset_);

    console.log( "ready!" );
    let ctr = $( "cytoscape-element[id=" + this.cyName + "]" )
    console.log(ctr);

    let policy_classes = this.getRoots(this.dataset_);
    let elts = this.getElements(this.dataset_);


    this.cy = cytoscape({

      container: ctr,

      minZoom: 1e-5,
      maxZoom: 1e5,

      elements: elts,
      style: [{
          selector: "node",
          style: {
            'background-color': 'data(color)',
            'color': 'data(textColor)',
            "text-opacity": 0.5,
            'border-width': 1,
            'height': cy_conf.node_shape.cellHeight,
            'width': cy_conf.node_shape.cellWidth,
            'label': 'data(id)',
            'content': 'data(id)',
            'shape': cy_conf.node_shape.shape,
            'text-valign': 'center',
            'text-halign': 'center',
            'text-wrap': 'wrap',
            'text-max-width': 100,
            "font-size": cy_conf.node_shape['font-size']
          }
        },
        {
          selector: "edge",
          style: {
            'label': 'data(label)',
            'line-style': 'data(linestyle)', // 'solid', //'dotted', //'dashed',
            'source-arrow-shape': 'data(sourceshape)',
            'source-arrow-color': 'data(linecolor)',
            'target-arrow-shape': 'data(targetshape)',
            'target-arrow-color': 'data(linecolor)',
            'line-color': 'data(linecolor)',
            'curve-style': 'bezier', //'bezier', //'segments'
            "width": "3px", // Edges width
            "font-size": cy_conf.edge_shape['font-size'],
            "font-weight": cy_conf.edge_shape['font-weight']
          }
        },
        {
          selector: ':selected',
          style: {
            "border-width": '10px',
            "border-color": cy_conf.selected.color,
            "lineColor": cy_conf.selected.color,
          }
        },
        {
          selector: '.withChildren',
          style: {
            "border-width": '5px',
            "border-style": 'dashed',
          }
        },
        {
          selector: ".mouseover",
          style: {
            "background-color": cy_conf.highlight.color,
            'border-width': "5px",
            'border-style': "double",
          }
        }
      ],
      layout: {
        name: 'breadthfirst',
        roots: policy_classes,
        transform: (node, position) => {
          return {
            x: (window.innerWidth - position.x) * cy_conf["x-scaling"],
            y: (window.innerHeight - position.y - (policy_classes.includes(node.data('id')) ? 0 : (Math.random() * cy_conf.node_shape.cellHeight))) * cy_conf["y-scaling"]
          };
        }
      }
    });


    //populating childrenData dictionary; graph starts expanded
    for (let x = 0; x < elts.nodes.length; x++) {
      let curNode = this.cy.$("#" + elts.nodes[x].data.id);
      let curId = curNode.data('id');

      //get its connectedEdges and connectedNodes
      let connectedEdges = curNode.connectedEdges(() => {
        //filter on connectedEdges
        return !curNode.target().anySame(curNode);
      });
      let connectedNodes = connectedEdges.targets();
      //and store that in childrenData
      this.childrenData.set(curId, {
        data: connectedNodes.union(connectedEdges),
        removed: false
      });

      if (Array.from(connectedNodes).length != 1) {
        curNode.addClass('withChildren');
      }
    }

    // console.log(this.childrenData);

    // register events listeners
    this.register();

    //Collapsing all nodes
    // policy_classes.map((x) => {
    //   this.collapse_node_all(this.cy.$("#" + x));
    // });
  }

  register() {
    this.cy.on('tap', 'node', (evt) => {
      let node = evt.target;
      console.log("tapped: ", node.id());
      console.log(this.childrenData.get(node.id()));
      this.expand_one_collapse_all(node);
      console.log(this.childrenData.get(node.id()));

    });

    this.cy.on('mouseover', 'node', (evt) => {
      let node = evt.target;
      node.addClass('mouseover');
    });

    this.cy.on('mouseout', 'node', (evt) => {
      let node = evt.target;
      node.removeClass('mouseover');
    });

    this.cy.on('cxttap', 'node', (evt) => {
      let node = evt.target;
      var childrenViewModal_childname = document.getElementById("childrenViewModal_childname");
      childrenViewModal_childname.innerText = "Node : " + node.data()['label'];

      // let outgoers_ = getChildNodes(childrenData, node.id());
      let curNode = node;

      let outgoers_ = node.outgoers((ele) => {
        if (ele.isEdge()) {
          return false;
        }
        if (ele.isNode()) {
          if (!ele.edgesWith(curNode).length) {
            return false;
          }
          if (this.cy.$("#" + ele.id()).edgesWith(this.cy.$("#" + curNode.id()))[0].data().group === "associations") {
            return false;
          }
        }

        //filter on connectedEdges
        return !curNode.target().anySame(curNode);
      });

      let child_lst = document.getElementById("childrenViewModal_childlist");
      let fragment = document.createDocumentFragment();

      child_lst.appendChild(fragment);
      modal.style.display = "block";
    });

    this.cy.on('tap', 'edge', (evt) => {
      var edge = evt.target;
      // document.getElementById("select").innerHTML = 'Selected: ' + edge.id();
      // edge.style({
      //   'line-color': cy_conf.highlight.color,
      //   'source-arrow-color': cy_conf.highlight.color,
      //   'target-arrow-color': cy_conf.highlight.color
      // });
      // if (edge.data('group') === "assignments") {
      //   edge.style({
      //     'line-color': 'red'
      //   });
      // } else {
      //   edge.style({
      //     'line-color': 'green'
      //   });
      // }
    });

    this.cy.on('mouseover', 'edge', (evt) => {
      var edge = evt.target;
      edge.style({
        'line-color': cy_conf.highlight.color,
        'source-arrow-color': cy_conf.highlight.color,
        'target-arrow-color': cy_conf.highlight.color
      });
    });

    this.cy.on('mouseout', 'edge', (evt) => {
      var edge = evt.target;
      if (!edge.selected()) {
        edge.style({
          'line-color': edge.data('linecolor'),
          'source-arrow-color': edge.data('linecolor'),
          'target-arrow-color': edge.data('linecolor')
        });
      }
    });

  }

  getChild(childDat, childName) {
    return childDat.get(childName);
  }

  getChildNodes(childDat, childName) {
    return childDat.get(childName).data.filter(x => x.isNode());
  }

  getChildEdges(childDat, childName) {
    return childDat.get(childName).data.filter(x => x.isEdge());
  }

  getChildren(nodeElt) {
    return nodeElt.outgoers((ele) => {
      if (ele.isEdge()) {
        return ele.data().group === "assignments";
      }
      if (ele.isNode()) {
        if (!ele.edgesWith(nodeElt).length) {
          return false;
        }
        return this.cy.$("#" + ele.id()).edgesWith(nodeElt)[0].data().group === "assignments";
      }
      //filter on connectedEdges
      return !curNode.target().anySame(curNode);
    });
  }

  getOutgoersByType(node_, type_) {
    return node_.outgoers((ele) => {
      if (ele.isEdge()) {
        if (ele.data().group === type_) {
          return false;
        }
      }
      if (ele.isNode()) {
        if (!ele.edgesWith(node_).length) {
          return false;
        }
        if (this.cy.$("#" + ele.id()).edgesWith(this.cy.$("#" + node_.id()))[0].data()['group'] === type_) {
          return false;
        }
      }
      return !node_.target().anySame(node_);
    });

  }

  getSelected() {
    if (this.cy.$(":selected").size() > 0) {
      return this.cy.$(":selected")[0].data()["id"];
    } else {
      return null;
    }
  }

  getAllByType(type) {
    /* Returns all elements of group associations or assignments */
    return this.cy.filter('[group = "' + type + '"]');
  }


  fetchAllChildren(nodeElt) {
    let connectedElt = this.getChildren(nodeElt);
    let toRemove = [];
    let trail = [];

    Array.prototype.push.apply(toRemove, connectedElt);

    connectedElt.forEach((nodeito) => {
      let elt = this.cy.$("#" + nodeito.id());
      if (elt.isNode()) {
        let conlvl2 = this.fetchAllChildren(elt);
        Array.prototype.push.apply(toRemove, conlvl2);
      }
    });
    return toRemove;
  }



  collapse_node_all(nodeo) {
    let nodesToRemove = this.fetchAllChildren(nodeo);
    this._collapse(nodesToRemove);
    this.childrenData.get(nodeo.id()).removed = true;
  }

  collapse_node_one(nodeo) {
    let nodesToRemove = this.getChildren(nodeo);
    this._collapse(nodesToRemove);
    this.childrenData.get(nodeo.id()).removed = true;
  }

  _collapse(nodesToRemove){
    nodesToRemove.map(x => x.hide());
    nodesToRemove.forEach((nodeito) => {
      if (nodeito.isNode()) {
        this.childrenData.get(nodeito.data('id')).removed = true;
      }
    });
  }

  expand_node_all(nodeo) {
    let childrenNodes = this.fetchAllChildren(nodeo);
    childrenNodes.map(x => x.show());
    childrenNodes.forEach((nodeito) => {
      if (nodeito.isNode()) {
        this.childrenData.get(nodeito.data('id')).removed = false;
      }
    });
    this.childrenData.get(nodeo.id()).removed = false;
  }

  expand_node_one(nodeo) {
    let childrenNodes = this.getChildren(nodeo);
    childrenNodes.map(x => x.show());
    childrenNodes.forEach((nodeito) => {
      if (nodeito.isNode()) {
        // this.childrenData.get(nodeito.data('id')).removed = false;
      }
    });
    this.childrenData.get(nodeo.id()).removed = false;
  }



  loadGraph(elements) {
    this.cy.json(JSON.parse(elements));
    this.cy.fit();
  }


  loadGraph1() {
    let elements = {

    };

    this.cy.json({
      elements: elements
    });
    this.cy.fit();
  }


  loadGraph2() {
    let elements = {

    };

    this.cy.json({
      elements: elements
    });
    this.cy.fit();
  }
}

customElements.define(CytoscapeElement.is, CytoscapeElement);