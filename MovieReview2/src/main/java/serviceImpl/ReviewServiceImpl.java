package serviceImpl;

import domain.ReviewDTO;
import domain.MovieWrapper;
import errormessage.ErrorMessage;
import exception.RequestInputException;
import mapper.LikeMapper;
import mapper.MemberMapper;
import mapper.MovieMapper;
import mapper.ReviewMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import response.BaseResponse;
import service.MemberService;
import service.ReviewService;
import util.Jwt;

import java.util.ArrayList;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    Jwt jwt;

    @Autowired
    MemberService memberService;

    @Autowired
    ReviewMapper reviewMapper;

    @Autowired
    MovieMapper movieMapper;

    @Autowired
    MemberMapper memberMapper;

    @Autowired
    LikeMapper likeMapper;

    @Override
    public BaseResponse createReview(MovieWrapper movieWrapper) throws Exception {
        //review null
        if(movieWrapper.getReview()==null)
            throw new RequestInputException(ErrorMessage.NULL_REVIEW);

        //jwt로 uid 확보
        Long uid=memberService.getLoginId();

        if(!movieMapper.linkExist(movieWrapper.getMovie().getLink())){
            //영화 저장 안되어있음 -> 저장
            movieMapper.registerMovie(movieWrapper.getMovie());
        }

        Long mid= movieMapper.getMid(movieWrapper.getMovie());
        String nickName=memberMapper.getNickName(uid);

        reviewMapper.registerReview(mid,uid,nickName, movieWrapper.getReview());
        return new BaseResponse("리뷰등록 성공", HttpStatus.OK);
    }

    @Override
    public ArrayList<ReviewDTO> getReviewsByMid(Long mid) {
        //여기서 반환할 ReviewDTO들의 likes 처리
        ArrayList<ReviewDTO> reviews=reviewMapper.getReviewsByMid(mid);
        for(ReviewDTO r:reviews){
            r.setLikes(likeMapper.countLikes(r.getRid()));
        }
        return reviews;
    }

    @Override
    public ArrayList<ReviewDTO> getReviewsByUid(Long uid) {
        //여기서 반환할 ReviewDTO들의 likes 처리
        ArrayList<ReviewDTO> reviews=reviewMapper.getReviewsByUid(uid);
        for(ReviewDTO r:reviews){
            r.setLikes(likeMapper.countLikes(r.getRid()));
        }
        return reviews;
    }

    @Override
    public BaseResponse deleteReview(Long rid) {
        reviewMapper.deleteReview(rid);
        return new BaseResponse("리뷰삭제 성공",HttpStatus.OK);
    }

    @Override
    public BaseResponse likeReview(Long rid) throws Exception {
        //좋아요 눌림 여부
        Long uid=memberService.getLoginId();
        boolean alreadyLike = likeMapper.likeExist(uid,rid);

        if(alreadyLike){
            //이미 좋아요가 눌러져 있으면 좋아요 취소
            likeMapper.deleteLike(uid,rid);

            return new BaseResponse("리뷰 좋아요 취소 성공",HttpStatus.OK);

        }else {
            //좋아요가 눌러져 있지 않으면 좋아요
            likeMapper.likeReview(uid,rid);

            return new BaseResponse("리뷰 좋아요 성공",HttpStatus.OK);
        }
    }

    @Override
    public BaseResponse updateReview(Long rid, String review) throws Exception {
        if(review==null)
            throw new RequestInputException(ErrorMessage.NULL_REVIEW);

        //uid 비교 불필요 ->front에서

        reviewMapper.updateReview(rid,review);

        return new BaseResponse("리뷰 업데이트 성공",HttpStatus.OK);
    }


}
