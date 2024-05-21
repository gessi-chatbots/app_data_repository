import json
from rdflib import Graph, URIRef, Literal, Namespace

SRJ_FILE = 'query-result.srj'
RESULT_FILE = 'dataset.ttl'

def load_file(file_name):
    with open(file_name, 'r') as file:
        return json.load(file)

def map_srj_to_graph(srj_data): 
    g = Graph()
    for result in srj_data['results']['bindings']:
            subj = URIRef(result['app']['value'])
            pred = URIRef(result['predicate']['value'])
            obj = result['object']
            
            if obj['type'] == 'uri':
                obj = URIRef(obj['value'])
            elif obj['type'] == 'literal':
                obj = Literal(obj['value'])
            
            g.add((subj, pred, obj))
    return g

def serialize_graph(graph): 
    graph.serialize(destination=RESULT_FILE, format='turtle')

def srj_to_ttl():
    srj_data = load_file(SRJ_FILE)
    graph = map_srj_to_graph(srj_data)
    return serialize_graph(graph)


def main(): 
    srj_to_ttl()

if __name__ == "__main__":
    main()
