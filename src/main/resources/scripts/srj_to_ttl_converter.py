import ijson
from rdflib import Graph, URIRef, Literal

SRJ_FILE = 'reviews.srj'
RESULT_FILE = 'reviewsResult.ttl'
SUBJECT = 'review'
# SUBJECT = 'subject'
PREDICATE = 'reviewPredicate'
# PREDICATE = 'predicate'
OBJECT = 'reviewObject'
# OBJECT = 'object'
LIMIT = 1000000

def map_srj_to_graph():
    with open(SRJ_FILE, 'r', encoding="utf8") as srj_file:
        g = Graph()
        counter = 0
        for binding in ijson.items(srj_file, "results.bindings.item"):
            if counter >= LIMIT:
                break
            subj = URIRef(binding[SUBJECT]['value'])
            pred = URIRef(binding[PREDICATE]['value'])
            obj = binding[OBJECT]
                
            if obj['type'] == 'uri':
                obj = URIRef(obj['value'])
            elif obj['type'] == 'literal':
                obj = Literal(obj['value'])
                
            g.add((subj, pred, obj))
            counter += 1
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
