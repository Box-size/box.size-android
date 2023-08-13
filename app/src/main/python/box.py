import io, json

from PIL import Image
import numpy as np
import cv2
import simplifier, findDot, calibration
from PIL.ExifTags import TAGS


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


def calculate_box_size(original : Image, crop : Image, params, xyxy, show=False):

    original = np.array(original)   # image를 cv2에서 사용 가능하게 변환
    original = cv2.cvtColor(original, cv2.COLOR_RGB2BGR)

    crop = np.array(crop)   # image를 cv2에서 사용 가능하게 변환
    crop = cv2.cvtColor(crop, cv2.COLOR_RGB2BGR)

    # box, xyxy = detector.detect(image) #박스 감지하고 crop 리턴

    crop, original_ratio = simplifier.simplify(crop) #배경 지우고, 외곽선 검출

    w, h, t = findDot.find(crop, original, xyxy, original_ratio, params, show=show)  #점 찾고 길이 반환 (show=True 시 과정 이미지 보임)

    print("최종 계산 결과 w, h, t:", w,h,t)

    return w, h, t


def calculate_camera_parameters(image : Image):
    params =  calibration.findParams(image)
    #print(rvec, dist, fx, fy, cx, cy, sep="\n")

    #TODO : 어떤 방식으로 서버에 저장할 지 정하기.
    return params
    # import pickle
    # paramFile = open("modules/params.bin",'wb')
    # pickle.dump(params, paramFile)
    # paramFile.close()




#TEST & DEBUG
def show(img):
    cv2.imshow("img",img)
    cv2.waitKey()

def main(original_image_data, crop_image_data, params, xyxy):
    original = Image.open(io.BytesIO(original_image_data))
    crop = Image.open(io.BytesIO(crop_image_data))
    original = rotate_image_with_exif(original)
    crop = rotate_image_with_exif(crop)

    try:
        json_params = params.replace("'", '"')
        params_dict = json.loads(json_params)
        params_list = [
            np.array(params_dict["rvec"]),
            np.array(params_dict["dist"]),
            params_dict["fx"],
            params_dict["fy"],
            params_dict["cx"],
            params_dict["cy"]
        ]
    except Exception:
        print("params 에러 -> 0 리턴함")
        return {'width': 0, 'height': 0, 'tall': 0}

    # try:
    width, height, tall = calculate_box_size(original, crop, params_list, xyxy, show=False)
    # except Exception:
    #     width, height, tall = 0, 0, 0
    result = {'width': width, 'height': height, 'tall': tall}
    print("최종 결과 주기")
    return result

if __name__ == '__main__':
    image = Image.open("modules/images_cali/bbox.jpg") # 이미지 테스트
    image_cali = Image.open("modules/images_cali/check3.jpg") #calibration
    image = rotate_image_with_exif(image)
    image_cali = rotate_image_with_exif(image_cali)
    params = calculate_camera_parameters(image_cali)
    print("calculate_camera_parameters 결과:", params)
    calculate_box_size(image, params, show=True)