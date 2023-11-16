package upc.edu.gessi.repo.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import upc.edu.gessi.repo.domain.serializer.CustomDateDeserializer;
import upc.edu.gessi.repo.domain.serializer.CustomDateSerializer;

import java.io.Serializable;
import java.util.Date;

public class Review implements Serializable {

    private static int counter = 0;
    private String reviewId;
    private String review;
    private String reply;
    private String userName;
    private Integer score;
    private String source;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMM dd, yyyy")
    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    private Date at;

    public String getReviewId() {
        if (reviewId != null)
            return reviewId;
        else return String.valueOf(++counter);
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Date getReviewDate() {
        return at;
    }

    public void setReviewDate(Date at) {this.at = at;}

}
