import cv2
from PIL import Image
import numpy as np
from PIL.ExifTags import TAGS
import io


def chess(gray):
    flags = cv2.CALIB_CB_ADAPTIVE_THRESH + cv2.CALIB_CB_NORMALIZE_IMAGE + cv2.CALIB_CB_FAST_CHECK
    return cv2.findChessboardCorners(gray, (4,7), flags)

def rotate_image_with_exif(image):
    
    try:
        # 이미지의 Exif 메타데이터 읽기
        exif = image._getexif()
        if exif is not None:
            for tag, value in exif.items():
                tag_name = TAGS.get(tag, tag)
                if tag_name == 'Orientation':
                    if value == 3:
                        image = image.rotate(180, expand=True)
                    elif value == 6:
                        image = image.rotate(270, expand=True)
                    elif value == 8:
                        image = image.rotate(90, expand=True)
                    break
    except AttributeError:
        pass  # Exif 정보가 없는 경우

    return image


def findRT(image):
    # termination criteria
    criteria = (cv2.TERM_CRITERIA_EPS + cv2.TERM_CRITERIA_MAX_ITER, 30, 0.001)
    # prepare object points, like (0,0,0), (1,0,0), (2,0,0) ....,(6,5,0)
    objp = np.zeros((7*4,3), np.float32)
    objp[:,:2] = np.mgrid[0:4,0:7].T.reshape(-1,2)
    # Arrays to store object points and image points from all the images.
    objpoints = [] # 3d point in real world space
    imgpoints = [] # 2d points in image plane.

    gray = cv2.cvtColor(image,cv2.COLOR_BGR2GRAY)

    #타임아웃 20초로 스케쥴 예약(멀티프로세싱 이용) -> 삭제
    ret, corners = chess(gray)

    # If found, add object points, image points (after refining them)
    if ret == True:
        objpoints.append(objp)
        corners2 = cv2.cornerSubPix(gray,corners,(11,11),(-1,-1),criteria)
        imgpoints.append(corners2)

        # Draw and display the corners
        # image = cv2.drawChessboardCorners(image, (4,7), corners2,ret)
        # image = cv2.resize(image,dsize=(800,600))
        # cv2.imshow('img',image)
        # cv2.waitKey(0)

        ret, mtx, dist, rvec, tvec = cv2.calibrateCamera(objpoints, imgpoints, gray.shape[::-1], None, None)
        fx, fy = mtx[0][0], mtx[1][1]
        cx, cy = mtx[0][2], mtx[1][2]
    else:
        raise ValueError
        #TODO : 실패하였을 경우 작성하기.

    #print(rvec, dist, fx, fy, cx, cy, sep="\n")
    return rvec[0], dist, fx, fy, cx, cy

def findParams(imageData):
    '''PIL이미지를 받아 camera parameters를 반환'''

    #cv2이미지로의 변환
    image_PIL = Image.open(io.BytesIO(imageData))
    image_PIL = rotate_image_with_exif(image_PIL)
    image_cv2 = np.array(image_PIL)
    image_cv2 = cv2.cvtColor(image_cv2, cv2.COLOR_RGB2BGR)
    try:
        rvec, dist, fx, fy, cx, cy = findRT(image_cv2)
    except ValueError:
        rvec, dist, fx, fy, cx, cy = np.array([0,0,0]), np.array([0,0,0,0,0]), 0, 0, 0, 0
    params = {
        "rvec": rvec.tolist(),
        "dist": dist.tolist(),
        "fx": fx,
        "fy": fy,
        "cx": cx,
        "cy": cy
    }
    return params
    

if __name__ == '__main__':
    
    image_PIL = Image.open('modules/images_cali/checkfail.jpg')
    print(findParams(image_PIL))
