/* jshint esversion: 6 */

export const elements = {
  "nodes": [
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
      "name": "super_oa",
      "type": "OA",
      "properties": {
        "namespace": "super"
      },
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
      "name": "RBAC_default_OA",
      "type": "OA",
      "properties": {
        "namespace": "RBAC"
      },
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
      "RBAC_rep",
      "super_oa"
    ],
    [
      "super",
      "super_ua2"
    ],
    [
      "RBAC_default_OA",
      "RBAC"
    ],
    [
      "super_pc_rep",
      "super_oa"
    ],
    [
      "super_oa",
      "super_pc"
    ],
    [
      "super_pc_default_OA",
      "super_pc"
    ],
    [
      "super_ua1",
      "super_pc"
    ],
    [
      "super_pc_default_UA",
      "super_pc"
    ],
    [
      "RBAC_default_UA",
      "RBAC"
    ],
    [
      "super_ua2",
      "super_pc"
    ],
    [
      "super",
      "super_ua1"
    ]
  ],
  "associations": [
    {
      "source": "super_ua1",
      "target": "super_pc_default_UA",
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
      "source": "super_ua1",
      "target": "super_oa",
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
      "source": "super_ua1",
      "target": "RBAC_default_OA",
      "operations": [
        "*"
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
}