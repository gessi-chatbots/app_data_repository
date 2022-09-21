package upc.edu.gessi.repo.domain;

import java.io.Serializable;

public class Review implements Serializable {

    private static int counter = 0;
    private String reviewId;
    private String snippet;
    private String reply;
    private String title;
    private Integer rating;
    private String source;


    public String getReviewId() {
        if (reviewId != null)
            return reviewId;
        else return String.valueOf(++counter);
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }


}
