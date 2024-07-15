import json

def generate_delete_query(review_ids):
    query_builder = []
    query_builder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n")
    query_builder.append("PREFIX schema: <https://schema.org/>\n")
    query_builder.append("DELETE\n")
    query_builder.append("WHERE {\n")

    for review_id in review_ids:
        query_builder.append(f"  ?s schema:identifier \"{review_id}\" .\n")
        query_builder.append("  ?s ?p ?o .\n")
        query_builder.append("  ?app schema:review ?s .\n")

    query_builder.append("}")

    return "".join(query_builder)

def save_query(query, filename):
    with open(filename, "w") as file:
        file.write(query)
def main():
    with open("OldestReviews.json", "r") as file:
        review_ids = json.load(file)

    sparql_query = generate_delete_query(review_ids)

    save_query(sparql_query, "delete_query.txt")



if __name__ == "__main__":
    main()
