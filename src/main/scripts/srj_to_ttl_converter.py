import ijson
from rdflib import Graph, URIRef, Literal

SRJ_FILE = '100KFirstReviews.srj'
RESULT_FILE = '100KFirstReviews.ttl'

def map_srj_to_graph():
    with open(SRJ_FILE, 'r', encoding="utf8") as srj_file:
        g = Graph()
        for binding in ijson.items(srj_file, "results.bindings.item"):
            subj = URIRef(binding['subject']['value'])
            pred = URIRef(binding['predicate']['value'])
            obj = binding['object']
                
            if obj['type'] == 'uri':
                obj = URIRef(obj['value'])
            elif obj['type'] == 'literal':
                obj = Literal(obj['value'])
                
            g.add((subj, pred, obj))
        return g

def serialize_graph(graph): 
    graph.serialize(destination=RESULT_FILE, format='turtle')

def srj_to_ttl():
    graph = map_srj_to_graph()
    return serialize_graph(graph)


def main(): 
    srj_to_ttl()

if __name__ == "__main__":
    main()
